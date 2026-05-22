/**
 * Dashboard application orchestration layer.
 *
 * @author Nilabro Saha
 */
import {
  createRobot,
  deleteRobot,
  fetchRobotById,
  fetchRobots
} from "../infrastructure/apiClient.js";
import { apiBase } from "../infrastructure/config.js";
import {
  addRobotButton,
  connectionEl,
  headerCells,
  intervalSelect,
  refreshNowButton,
  sidebarBackdropEl,
  sidebarCloseButton,
  sidebarContentEl,
  tbody,
  toggleLiveButton
} from "../presentation/dom.js";
import {
  closeSidebar,
  openSidebar,
  showSidebarError,
  showSidebarLoading
} from "../presentation/sidebarView.js";
import { renderRows, updateHeaderSortIndicators } from "../presentation/tableView.js";
import { dashboardState } from "../domain/dashboardState.js";
import { formatLocalTimestamp } from "../domain/formatters.js";

dashboardState.refreshIntervalMs = Number(intervalSelect.value);

/**
 * Refreshes selected robot sidebar content with newest backend state.
 *
 * @param {Array<object>} robots latest fleet rows from list endpoint
 * @returns {Promise<void>}
 */
async function syncSidebarWithData(robots) {
  if (!dashboardState.selectedRobotId) {
    return;
  }

  const selectedRobot = robots.find((robot) => robot.robotId === dashboardState.selectedRobotId);
  if (!selectedRobot) {
    closeSidebar(dashboardState);
    return;
  }

  try {
    const latestRobotDetails = await fetchRobotById(dashboardState.selectedRobotId);
    openSidebar(latestRobotDetails, dashboardState);
  } catch (error) {
    showSidebarError(dashboardState.selectedRobotId, `Unable to load details: ${error.message}`);
  }
}

/**
 * Handles click on a fleet table row and opens corresponding details sidebar.
 *
 * @param {MouseEvent} event click event from robots table body
 * @returns {Promise<void>}
 */
async function handleRobotRowClick(event) {
  const row = event.target.closest("tr[data-robot-id]");
  if (!row) {
    return;
  }

  const robotId = row.dataset.robotId;
  const selectedRobot = dashboardState.latestRobots.find((robot) => robot.robotId === robotId);
  if (!selectedRobot) {
    return;
  }

  showSidebarLoading(robotId, dashboardState);
  try {
    const robotDetails = await fetchRobotById(robotId);
    openSidebar(robotDetails, dashboardState);
  } catch (error) {
    showSidebarError(robotId, `Unable to load details: ${error.message}`);
  }

  renderRows(dashboardState.latestRobots, dashboardState, closeSidebar);
}

/**
 * Pulls fleet data from API and updates table/sidebar/connection status.
 *
 * @returns {Promise<void>}
 */
async function refresh() {
  try {
    const robots = await fetchRobots(dashboardState.sortBy, dashboardState.sortDir);
    dashboardState.latestRobots = Array.isArray(robots) ? robots : [];
    renderRows(robots, dashboardState, closeSidebar);
    await syncSidebarWithData(robots);
    dashboardState.lastRefreshAt = formatLocalTimestamp(new Date());
    const modeText = dashboardState.liveRefreshEnabled
      ? `Live polling ${apiBase}/api/robot-statuses?sort=${dashboardState.sortBy},${dashboardState.sortDir} every ${dashboardState.refreshIntervalMs / 1000}s | Last refresh: ${dashboardState.lastRefreshAt}`
      : `Live refresh is paused. API base: ${apiBase} | sort=${dashboardState.sortBy},${dashboardState.sortDir} | Last refresh: ${dashboardState.lastRefreshAt}`;
    connectionEl.textContent = modeText;
  } catch (error) {
    connectionEl.textContent = `Unable to reach API at ${apiBase}. ${error.message} | Last refresh: ${dashboardState.lastRefreshAt}`;
    connectionEl.classList.remove("muted");
    connectionEl.style.color = "#dc2626";
  }
}

/**
 * Recreates periodic refresh timer based on current live-refresh settings.
 */
function startLiveTimer() {
  if (dashboardState.refreshTimerId) {
    clearInterval(dashboardState.refreshTimerId);
  }

  if (dashboardState.liveRefreshEnabled) {
    dashboardState.refreshTimerId = setInterval(refresh, dashboardState.refreshIntervalMs);
  }
}

/**
 * Synchronizes toggle button and interval dropdown UI with state.
 */
function updateLiveControls() {
  toggleLiveButton.textContent = dashboardState.liveRefreshEnabled
    ? "Live Refresh: ON"
    : "Live Refresh: OFF";
  intervalSelect.disabled = !dashboardState.liveRefreshEnabled;
}

/**
 * Wires sortable table header click handlers.
 */
function wireHeaderSorting() {
  headerCells.forEach((headerCell) => {
    headerCell.addEventListener("click", async () => {
      const clickedSortField = headerCell.dataset.sortField;
      if (!clickedSortField) {
        return;
      }

      if (dashboardState.sortBy === clickedSortField) {
        dashboardState.sortDir = dashboardState.sortDir === "asc" ? "desc" : "asc";
      } else {
        dashboardState.sortBy = clickedSortField;
        dashboardState.sortDir = "asc";
      }

      updateHeaderSortIndicators(dashboardState);
      await refresh();
    });
  });
}

/**
 * Wires all interactive UI actions (toggle, refresh, create, delete, sidebar close).
 */
function wireActions() {
  toggleLiveButton.addEventListener("click", async () => {
    dashboardState.liveRefreshEnabled = !dashboardState.liveRefreshEnabled;
    updateLiveControls();
    startLiveTimer();
    await refresh();
  });

  intervalSelect.addEventListener("change", () => {
    dashboardState.refreshIntervalMs = Number(intervalSelect.value);
    startLiveTimer();
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

  sidebarContentEl.addEventListener("click", async (event) => {
    const deleteButton = event.target.closest("#delete-robot");
    if (!deleteButton) {
      return;
    }

    const robotId = deleteButton.dataset.robotId;
    if (!robotId) {
      return;
    }

    const confirmed = window.confirm(`Delete robot ${robotId}?`);
    if (!confirmed) {
      return;
    }

    deleteButton.disabled = true;
    deleteButton.textContent = "Deleting...";

    try {
      const deletionResponse = await deleteRobot(robotId);
      connectionEl.classList.add("muted");
      connectionEl.style.color = "";
      connectionEl.textContent = `Delete request accepted for ${deletionResponse.robotId}. Awaiting simulator removal lifecycle event...`;
      await refresh();
    } catch (error) {
      deleteButton.disabled = false;
      deleteButton.textContent = "Delete Robot";
      window.alert(`Unable to delete robot. ${error.message}`);
    }
  });

  tbody.addEventListener("click", handleRobotRowClick);
  sidebarCloseButton.addEventListener("click", () => closeSidebar(dashboardState));
  sidebarBackdropEl.addEventListener("click", () => closeSidebar(dashboardState));
}

/**
 * Initializes dashboard behavior and starts first refresh + timer loop.
 *
 * @returns {Promise<void>}
 */
export async function initDashboardController() {
  wireHeaderSorting();
  wireActions();
  updateLiveControls();
  updateHeaderSortIndicators(dashboardState);
  await refresh();
  startLiveTimer();
}
