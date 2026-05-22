/**
 * Fleet table view rendering utilities.
 *
 * @author Nilabro Saha
 */
import { headerCells, tbody } from "./dom.js";
import { formatLocalTimestamp } from "../domain/formatters.js";

/**
 * Renders fleet rows and applies selection highlighting.
 *
 * @param {Array<object>} robots fleet row payloads
 * @param {object} state mutable dashboard state object
 * @param {Function} closeSidebar sidebar close callback
 */
export function renderRows(robots, state, closeSidebar) {
  if (!Array.isArray(robots) || robots.length === 0) {
    closeSidebar(state);
    tbody.innerHTML = `<tr><td colspan="8">No telemetry received yet.</td></tr>`;
    return;
  }

  tbody.innerHTML = robots
    .map((robot) => {
      const status = robot.status ?? "UNKNOWN";
      const lifecycle = robot.lifecycleStatus ?? "UNKNOWN";
      const timestamp = formatLocalTimestamp(robot.timestamp);
      const selectedClass = state.selectedRobotId === robot.robotId ? "selected-row" : "";
      return `
        <tr class="${selectedClass}" data-robot-id="${robot.robotId}">
          <td>${robot.robotId}</td>
          <td>${robot.displayName ?? "-"}</td>
          <td><span class="lifecycle-${lifecycle}">${lifecycle}</span></td>
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

/**
 * Updates sort indicator classes on sortable table headers.
 *
 * @param {object} state mutable dashboard state object
 */
export function updateHeaderSortIndicators(state) {
  headerCells.forEach((headerCell) => {
    const isActive = headerCell.dataset.sortField === state.sortBy;
    headerCell.classList.toggle("sort-active", isActive);
    headerCell.classList.toggle("sort-desc", isActive && state.sortDir === "desc");
  });
}
