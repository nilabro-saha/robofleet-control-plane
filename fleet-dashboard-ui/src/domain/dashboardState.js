/**
 * Dashboard state model defaults.
 *
 * @author Nilabro Saha
 */
export const dashboardState = {
  liveRefreshEnabled: true,
  refreshIntervalMs: 1000,
  refreshTimerId: null,
  lastRefreshAt: "Never",
  selectedRobotId: null,
  latestRobots: [],
  sortBy: "robotId",
  sortDir: "asc"
};
