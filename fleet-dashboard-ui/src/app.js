const connectionEl = document.getElementById("connection");
const tbody = document.getElementById("robots-body");
const toggleLiveButton = document.getElementById("toggle-live");
const intervalSelect = document.getElementById("interval-select");
const refreshNowButton = document.getElementById("refresh-now");

const params = new URLSearchParams(window.location.search);
const apiBase = params.get("apiBase") ?? "http://localhost:8080";

let liveRefreshEnabled = true;
let refreshIntervalMs = Number(intervalSelect.value);
let refreshTimerId = null;
let lastRefreshAt = "Never";

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
  const response = await fetch(`${apiBase}/api/robots`, {
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
    tbody.innerHTML = `<tr><td colspan="6">No telemetry received yet.</td></tr>`;
    return;
  }

  tbody.innerHTML = robots
    .map((robot) => {
      const status = robot.status ?? "UNKNOWN";
      const timestamp = formatLocalTimestamp(robot.timestamp);
      return `
        <tr>
          <td>${robot.robotId}</td>
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

async function refresh() {
  try {
    const robots = await fetchRobots();
    renderRows(robots);
    lastRefreshAt = formatLocalTimestamp(new Date());
    const modeText = liveRefreshEnabled
      ? `Live polling ${apiBase}/api/robots every ${refreshIntervalMs / 1000}s | Last refresh: ${lastRefreshAt}`
      : `Live refresh is paused. API base: ${apiBase} | Last refresh: ${lastRefreshAt}`;
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

refreshNowButton.addEventListener("click", async () => {
  await refresh();
});

updateLiveControls();
await refresh();
startLiveTimer();
