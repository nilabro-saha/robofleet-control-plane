import test from "node:test";
import assert from "node:assert/strict";

import { formatLocalTimestamp } from "../../src/domain/formatters.js";

test("formatLocalTimestamp returns '-' for empty input", () => {
  assert.equal(formatLocalTimestamp(null), "-");
  assert.equal(formatLocalTimestamp(undefined), "-");
});

test("formatLocalTimestamp returns original value when date is invalid", () => {
  const invalid = "not-a-date";
  assert.equal(formatLocalTimestamp(invalid), invalid);
});

test("formatLocalTimestamp formats valid Date using 12-hour clock", () => {
  const date = new Date(2026, 5, 15, 13, 4, 5);
  const result = formatLocalTimestamp(date);

  assert.ok(result.includes("2026"));
  assert.ok(result.endsWith("1:04:05 pm"));
});
