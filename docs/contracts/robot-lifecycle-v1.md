# Contract: `robot.lifecycle` (v1)

## Topic

- `robot.lifecycle`

## Purpose

Carries robot lifecycle orchestration commands/events between fleet-state-service and robot-simulator.

## JSON schema (logical)

```json
{
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

- `eventName`: required, fixed value `robot-lifecycle-changed`.
- `correlationId`: required in Phase 2 baseline.
- `x`, `y`, `battery`, `status`: optional, used for state-carrying events (especially rehydration).
- `eventType`: drives handler routing logic.

## Compatibility

- Consumers should ignore unknown additive fields.
- New event types must be added conservatively with handler fallback behavior.
