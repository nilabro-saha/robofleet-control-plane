/**
 * Fleet dashboard API client.
 *
 * @author Nilabro Saha
 */
import { apiBase } from "./config.js";

/**
 * Validates HTTP responses and raises a descriptive error when request fails.
 *
 * @param {Response} response fetch response object
 * @param {string} messagePrefix error message prefix
 * @returns {Promise<Response>} resolved response when status is OK
 */
async function ensureOk(response, messagePrefix) {
  if (!response.ok) {
    throw new Error(`${messagePrefix} with status ${response.status}`);
  }
  return response;
}

/**
 * Sends a create command for a new robot.
 *
 * @param {string} displayName operator-provided robot display name
 * @returns {Promise<object>} accepted create response payload
 */
export async function createRobot(displayName) {
  const response = await fetch(`${apiBase}/api/robots`, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ displayName })
  });

  await ensureOk(response, "Create robot failed");
  return response.json();
}

/**
 * Sends delete request for a robot lifecycle.
 *
 * @param {string} robotId robot identifier
 * @returns {Promise<object>} accepted delete response payload
 */
export async function deleteRobot(robotId) {
  const response = await fetch(`${apiBase}/api/robots/${encodeURIComponent(robotId)}`, {
    method: "DELETE",
    headers: {
      Accept: "application/json"
    }
  });

  await ensureOk(response, "Delete robot failed");
  return response.json();
}

/**
 * Fetches latest state projection for a single robot.
 *
 * @param {string} robotId robot identifier
 * @returns {Promise<object>} latest robot state payload
 */
export async function fetchRobotById(robotId) {
  const response = await fetch(`${apiBase}/api/robot-statuses/${encodeURIComponent(robotId)}`, {
    headers: {
      Accept: "application/json"
    }
  });

  await ensureOk(response, `Robot ${robotId} request failed`);
  return response.json();
}

/**
 * Fetches current fleet snapshot with server-side sorting.
 *
 * @param {string} sortBy sortable field name
 * @param {string} sortDir sort direction (`asc` or `desc`)
 * @returns {Promise<Array<object>>} list of robot state rows
 */
export async function fetchRobots(sortBy, sortDir) {
  const query = new URLSearchParams({
    sort: `${sortBy},${sortDir}`,
    size: "100"
  });

  const response = await fetch(`${apiBase}/api/robot-statuses?${query.toString()}`, {
    headers: {
      Accept: "application/json"
    }
  });

  await ensureOk(response, "API request failed");
  return response.json();
}
