const connectionEl = document.getElementById("connection");
const tbody = document.getElementById("robots-body");
const headerCells = document.querySelectorAll("th[data-sort-field]");
const addRobotButton = document.getElementById("add-robot");
const toggleLiveButton = document.getElementById("toggle-live");
const intervalSelect = document.getElementById("interval-select");
const refreshNowButton = document.getElementById("refresh-now");
const sidebarEl = document.getElementById("robot-sidebar");
const sidebarBackdropEl = document.getElementById("sidebar-backdrop");
const sidebarCloseButton = document.getElementById("sidebar-close");
const sidebarTitleEl = document.getElementById("sidebar-title");
const sidebarContentEl = document.getElementById("sidebar-content");

const params = new URLSearchParams(window.location.search);
const apiBase = params.get("apiBase") ?? "http://localhost:8080";

/**
 * Intent of this module:
 * Keep the operator experience framework-light while still giving practical
 * control over how live fleet state is refreshed and inspected.
 */

let liveRefreshEnabled = true;
let refreshIntervalMs = Number(intervalSelect.value);
let refreshTimerId = null;
let lastRefreshAt = "Never";
let selectedRobotId = null;
let latestRobots = [];
let sortBy = "robotId";
let sortDir = "asc";

async function createRobot(displayName) {
  const response = await fetch(`${apiBase}/api/robots`, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ displayName })
  });

  if (!response.ok) {
    throw new Error(`Create robot failed with status ${response.status}`);
  }

  return response.json();
}

async function fetchRobotById(robotId) {
  const response = await fetch(`${apiBase}/api/robot-statuses/${encodeURIComponent(robotId)}`, {
    headers: {
      Accept: "application/json"
    }
  });

  if (!response.ok) {
    throw new Error(`Robot ${robotId} request failed with status ${response.status}`);
  }

  return response.json();
}

function formatLocalTimestamp(dateInput) {
  if (!dateInput) {
    return "-";
  }

  const date = dateInput instanceof Date ? dateInput : new Date(dateInput);
  if (Number.isNaN(date.getTime())) {
    return String(dateInput);
  }

  const day = String(date.getDate()).padStart(2, "0");
  const month = date.toLocaleString(undefined, { month: "short" });
  const year = date.getFullYear();

  let hours = date.getHours();
  const minutes = String(date.getMinutes()).padStart(2, "0");
  const seconds = String(date.getSeconds()).padStart(2, "0");
  const meridiem = hours >= 12 ? "pm" : "am";

  hours = hours % 12;
  hours = hours === 0 ? 12 : hours;

  return `${day}-${month}-${year} ${hours}:${minutes}:${seconds} ${meridiem}`;
}

async function fetchRobots() {
  const query = new URLSearchParams({
    sort: `${sortBy},${sortDir}`,
    size: "100"
  });

  const response = await fetch(`${apiBase}/api/robot-statuses?${query.toString()}`, {
    headers: {
      Accept: "application/json"
    }
  });

  if (!response.ok) {
    throw new Error(`API request failed with status ${response.status}`);
  }

  return response.json();
}

function renderRows(robots) {
  if (!Array.isArray(robots) || robots.length === 0) {
    closeSidebar();
    tbody.innerHTML = `<tr><td colspan="8">No telemetry received yet.</td></tr>`;
    return;
  }

  tbody.innerHTML = robots
    .map((robot) => {
      const status = robot.status ?? "UNKNOWN";
      const timestamp = formatLocalTimestamp(robot.timestamp);
      const selectedClass = selectedRobotId === robot.robotId ? "selected-row" : "";
      return `
        <tr class="${selectedClass}" data-robot-id="${robot.robotId}">
          <td>${robot.robotId}</td>
          <td>${robot.displayName ?? "-"}</td>
          <td>${robot.lifecycleStatus ?? "UNKNOWN"}</td>
          <td>${Number(robot.x).toFixed(2)}</td>
          <td>${Number(robot.y).toFixed(2)}</td>
          <td>${Number(robot.battery).toFixed(2)}%</td>
          <td><span class="status-${status}">${status}</span></td>
          <td>${timestamp}</td>
        </tr>
      `;
    })
    .join("");
}

function updateHeaderSortIndicators() {
  headerCells.forEach((headerCell) => {
    const isActive = headerCell.dataset.sortField === sortBy;
    headerCell.classList.toggle("sort-active", isActive);
    headerCell.classList.toggle("sort-desc", isActive && sortDir === "desc");
  });
}

function formatRobotDetails(robot) {
  const timestamp = formatLocalTimestamp(robot.timestamp);
  return `
    <dl class="details-grid">
      <dt>Robot ID</dt><dd>${robot.robotId}</dd>
      <dt>Display Name</dt><dd>${robot.displayName ?? "-"}</dd>
      <dt>Lifecycle</dt><dd>${robot.lifecycleStatus ?? "UNKNOWN"}</dd>
      <dt>Status</dt><dd>${robot.status ?? "UNKNOWN"}</dd>
      <dt>Battery</dt><dd>${Number(robot.battery).toFixed(2)}%</dd>
      <dt>X</dt><dd>${Number(robot.x).toFixed(2)}</dd>
      <dt>Y</dt><dd>${Number(robot.y).toFixed(2)}</dd>
      <dt>Timestamp</dt><dd>${timestamp}</dd>
    </dl>
  `;
}

function openSidebar(robot) {
  selectedRobotId = robot.robotId;
  sidebarTitleEl.textContent = robot.displayName
    ? `${robot.displayName} (${robot.robotId})`
    : `Robot ${robot.robotId}`;
  sidebarContentEl.innerHTML = formatRobotDetails(robot);
  sidebarEl.classList.add("open");
  sidebarEl.setAttribute("aria-hidden", "false");
  sidebarBackdropEl.classList.add("visible");
}

function showSidebarLoading(robotId) {
  selectedRobotId = robotId;
  sidebarTitleEl.textContent = `Robot ${robotId}`;
  sidebarContentEl.innerHTML = '<p class="muted">Loading robot details...</p>';
  sidebarEl.classList.add("open");
  sidebarEl.setAttribute("aria-hidden", "false");
  sidebarBackdropEl.classList.add("visible");
}

function showSidebarError(robotId, message) {
  sidebarTitleEl.textContent = `Robot ${robotId}`;
  sidebarContentEl.innerHTML = `<p class="muted">${message}</p>`;
  sidebarEl.classList.add("open");
  sidebarEl.setAttribute("aria-hidden", "false");
  sidebarBackdropEl.classList.add("visible");
}

function closeSidebar() {
  selectedRobotId = null;
  sidebarEl.classList.remove("open");
  sidebarEl.setAttribute("aria-hidden", "true");
  sidebarBackdropEl.classList.remove("visible");
}

async function syncSidebarWithData(robots) {
  if (!selectedRobotId) {
    return;
  }

  const selectedRobot = robots.find((robot) => robot.robotId === selectedRobotId);
  if (!selectedRobot) {
    closeSidebar();
    return;
  }

  try {
    const latestRobotDetails = await fetchRobotById(selectedRobotId);
    openSidebar(latestRobotDetails);
  } catch (error) {
    showSidebarError(selectedRobotId, `Unable to load details: ${error.message}`);
  }
}

async function handleRobotRowClick(event) {
  const row = event.target.closest("tr[data-robot-id]");
  if (!row) {
    return;
  }

  const robotId = row.dataset.robotId;
  const selectedRobot = latestRobots.find((robot) => robot.robotId === robotId);
  if (!selectedRobot) {
    return;
  }

  showSidebarLoading(robotId);
  try {
    const robotDetails = await fetchRobotById(robotId);
    openSidebar(robotDetails);
  } catch (error) {
    showSidebarError(robotId, `Unable to load details: ${error.message}`);
  }

  renderRows(latestRobots);
}

async function refresh() {
  try {
    const robots = await fetchRobots();
    latestRobots = Array.isArray(robots) ? robots : [];
    renderRows(robots);
    await syncSidebarWithData(robots);
    lastRefreshAt = formatLocalTimestamp(new Date());
    const modeText = liveRefreshEnabled
      ? `Live polling ${apiBase}/api/robot-statuses?sort=${sortBy},${sortDir} every ${refreshIntervalMs / 1000}s | Last refresh: ${lastRefreshAt}`
      : `Live refresh is paused. API base: ${apiBase} | sort=${sortBy},${sortDir} | Last refresh: ${lastRefreshAt}`;
    connectionEl.textContent = modeText;
  } catch (error) {
    connectionEl.textContent = `Unable to reach API at ${apiBase}. ${error.message} | Last refresh: ${lastRefreshAt}`;
    connectionEl.classList.remove("muted");
    connectionEl.style.color = "#dc2626";
  }
}

function startLiveTimer() {
  if (refreshTimerId) {
    clearInterval(refreshTimerId);
  }

  if (liveRefreshEnabled) {
    refreshTimerId = setInterval(refresh, refreshIntervalMs);
  }
}

function updateLiveControls() {
  toggleLiveButton.textContent = liveRefreshEnabled
    ? "Live Refresh: ON"
    : "Live Refresh: OFF";
  intervalSelect.disabled = !liveRefreshEnabled;
}

toggleLiveButton.addEventListener("click", async () => {
  liveRefreshEnabled = !liveRefreshEnabled;
  updateLiveControls();
  startLiveTimer();
  await refresh();
});

intervalSelect.addEventListener("change", () => {
  refreshIntervalMs = Number(intervalSelect.value);
  startLiveTimer();
});

headerCells.forEach((headerCell) => {
  headerCell.addEventListener("click", async () => {
    const clickedSortField = headerCell.dataset.sortField;
    if (!clickedSortField) {
      return;
    }

    if (sortBy === clickedSortField) {
      sortDir = sortDir === "asc" ? "desc" : "asc";
    } else {
      sortBy = clickedSortField;
      sortDir = "asc";
    }

    updateHeaderSortIndicators();
    await refresh();
  });
});

refreshNowButton.addEventListener("click", async () => {
  await refresh();
});

addRobotButton.addEventListener("click", async () => {
  const displayNameInput = window.prompt("Enter display name for new robot:");
  if (displayNameInput === null) {
    return;
  }

  const displayName = displayNameInput.trim();
  if (!displayName) {
    window.alert("Display name is required.");
    return;
  }

  try {
    const createdRobot = await createRobot(displayName);
    connectionEl.classList.add("muted");
    connectionEl.style.color = "";
    connectionEl.textContent = `Create request accepted for ${createdRobot.displayName} (${createdRobot.robotId}). Awaiting simulator lifecycle event...`;
    await refresh();
  } catch (error) {
    window.alert(`Unable to create robot. ${error.message}`);
  }
});

tbody.addEventListener("click", handleRobotRowClick);

sidebarCloseButton.addEventListener("click", closeSidebar);
sidebarBackdropEl.addEventListener("click", closeSidebar);

updateLiveControls();
updateHeaderSortIndicators();
await refresh();
startLiveTimer();
