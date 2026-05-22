# Contract: `robot.lifecycle` (v1)

## Topic

- `robot.lifecycle`

## Purpose

Carries robot lifecycle orchestration events between `fleet-state-service` and `robot-simulator`.

## JSON schema (logical)

```json
{
  "schemaVersion": "string",
  "eventName": "string",
  "correlationId": "string",
  "robotId": "string",
  "x": "number|null",
  "y": "number|null",
  "battery": "number|null",
  "status": "string|null",
  "eventType": "enum",
  "timestamp": "ISO-8601 instant"
}
```

## Event types

- `CREATE_PENDING`
- `REHYDRATE_ACTIVE`
- `CREATED`
- `DELETE_PENDING`
- `REMOVED`

## Field notes

- `schemaVersion`: required, fixed value `v1`.
- `eventName`: required, fixed value `robot-lifecycle-changed`.
- `correlationId`: required in Phase 2 baseline.
- `x`, `y`, `battery`, `status`: optional; mainly used when events carry state (especially during rehydration).
- `eventType`: determines which handler path should run.

## Compatibility

- Consumers should ignore unknown additive fields.
- New event types should be introduced carefully, with fallback handling in consumers.
