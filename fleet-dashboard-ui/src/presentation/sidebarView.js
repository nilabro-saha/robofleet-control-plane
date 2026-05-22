/**
 * Sidebar view rendering utilities.
 *
 * @author Nilabro Saha
 */
import {
  sidebarBackdropEl,
  sidebarContentEl,
  sidebarEl,
  sidebarTitleEl
} from "./dom.js";
import { formatLocalTimestamp } from "../domain/formatters.js";

/**
 * Builds HTML content for robot details sidebar.
 *
 * @param {object} robot latest robot detail payload
 * @returns {string} sidebar HTML template
 */
function formatRobotDetails(robot) {
  const timestamp = formatLocalTimestamp(robot.timestamp);
  const lifecycleStatus = robot.lifecycleStatus ?? "UNKNOWN";
  const isDeletePending = lifecycleStatus === "DELETE_PENDING";
  return `
    <dl class="details-grid">
      <dt>Robot ID</dt><dd>${robot.robotId}</dd>
      <dt>Display Name</dt><dd>${robot.displayName ?? "-"}</dd>
      <dt>Lifecycle</dt><dd>${lifecycleStatus}</dd>
      <dt>Status</dt><dd>${robot.status ?? "UNKNOWN"}</dd>
      <dt>Battery</dt><dd>${Number(robot.battery).toFixed(2)}%</dd>
      <dt>X</dt><dd>${Number(robot.x).toFixed(2)}</dd>
      <dt>Y</dt><dd>${Number(robot.y).toFixed(2)}</dd>
      <dt>Timestamp</dt><dd>${timestamp}</dd>
    </dl>
    <div class="sidebar-actions">
      <button
        id="delete-robot"
        class="button-delete"
        type="button"
        data-robot-id="${robot.robotId}"
        ${isDeletePending ? "disabled" : ""}
      >
        ${isDeletePending ? "Deletion Pending" : "Delete Robot"}
      </button>
    </div>
  `;
}

/**
 * Opens sidebar with fully rendered robot details.
 *
 * @param {object} robot latest robot detail payload
 * @param {object} state mutable dashboard state object
 */
export function openSidebar(robot, state) {
  state.selectedRobotId = robot.robotId;
  sidebarTitleEl.textContent = robot.displayName
    ? `${robot.displayName} (${robot.robotId})`
    : `Robot ${robot.robotId}`;
  sidebarContentEl.innerHTML = formatRobotDetails(robot);
  sidebarEl.classList.add("open");
  sidebarEl.setAttribute("aria-hidden", "false");
  sidebarBackdropEl.classList.add("visible");
}

/**
 * Opens sidebar in loading mode while details request is in progress.
 *
 * @param {string} robotId selected robot identifier
 * @param {object} state mutable dashboard state object
 */
export function showSidebarLoading(robotId, state) {
  state.selectedRobotId = robotId;
  sidebarTitleEl.textContent = `Robot ${robotId}`;
  sidebarContentEl.innerHTML = '<p class="muted">Loading robot details...</p>';
  sidebarEl.classList.add("open");
  sidebarEl.setAttribute("aria-hidden", "false");
  sidebarBackdropEl.classList.add("visible");
}

/**
 * Opens sidebar with an error message when details fetch fails.
 *
 * @param {string} robotId selected robot identifier
 * @param {string} message displayable error message
 */
export function showSidebarError(robotId, message) {
  sidebarTitleEl.textContent = `Robot ${robotId}`;
  sidebarContentEl.innerHTML = `<p class="muted">${message}</p>`;
  sidebarEl.classList.add("open");
  sidebarEl.setAttribute("aria-hidden", "false");
  sidebarBackdropEl.classList.add("visible");
}

/**
 * Closes sidebar and clears selected robot tracking from state.
 *
 * @param {object} state mutable dashboard state object
 */
export function closeSidebar(state) {
  state.selectedRobotId = null;
  sidebarEl.classList.remove("open");
  sidebarEl.setAttribute("aria-hidden", "true");
  sidebarBackdropEl.classList.remove("visible");
}
