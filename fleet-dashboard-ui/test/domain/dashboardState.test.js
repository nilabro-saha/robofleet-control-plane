import test from "node:test";
import assert from "node:assert/strict";

import { dashboardState } from "../../src/domain/dashboardState.js";

test("dashboardState exposes expected defaults", () => {
  assert.equal(dashboardState.liveRefreshEnabled, true);
  assert.equal(dashboardState.refreshIntervalMs, 1000);
  assert.equal(dashboardState.lastRefreshAt, "Never");
  assert.equal(dashboardState.selectedRobotId, null);
  assert.deepEqual(dashboardState.latestRobots, []);
  assert.equal(dashboardState.sortBy, "robotId");
  assert.equal(dashboardState.sortDir, "asc");
});
