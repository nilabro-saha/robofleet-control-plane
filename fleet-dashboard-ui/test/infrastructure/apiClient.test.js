import test from "node:test";
import assert from "node:assert/strict";

import {
  createRobot,
  deleteRobot,
  fetchRobotById,
  fetchRobots
} from "../../src/infrastructure/apiClient.js";

test("fetchRobots builds sorted request and returns payload", async () => {
  const originalFetch = global.fetch;
  try {
    global.fetch = async (url) => {
      assert.ok(String(url).includes("/api/robot-statuses?"));
      assert.ok(String(url).includes("sort=robotId%2Casc"));
      assert.ok(String(url).includes("size=100"));
      return {
        ok: true,
        json: async () => [{ robotId: "robot-1" }]
      };
    };

    const result = await fetchRobots("robotId", "asc");
    assert.deepEqual(result, [{ robotId: "robot-1" }]);
  } finally {
    global.fetch = originalFetch;
  }
});

test("fetchRobotById throws on non-OK response", async () => {
  const originalFetch = global.fetch;
  try {
    global.fetch = async () => ({ ok: false, status: 500 });
    await assert.rejects(
      () => fetchRobotById("robot-42"),
      /Robot robot-42 request failed with status 500/
    );
  } finally {
    global.fetch = originalFetch;
  }
});

test("createRobot and deleteRobot send expected HTTP methods", async () => {
  const originalFetch = global.fetch;
  const calls = [];
  try {
    global.fetch = async (url, options) => {
      calls.push({ url: String(url), options });
      return {
        ok: true,
        json: async () => ({ robotId: "robot-1", displayName: "Demo" })
      };
    };

    await createRobot("Demo");
    await deleteRobot("robot-1");

    assert.equal(calls[0].options.method, "POST");
    assert.equal(calls[1].options.method, "DELETE");
  } finally {
    global.fetch = originalFetch;
  }
});
