# Contract: `robot.telemetry` (v1)

## Topic

- `robot.telemetry`

## Purpose

Carries latest robot runtime telemetry from simulator/edge producers to fleet-state-service for state materialization.

## JSON schema (logical)

```json
{
  "eventName": "string",
  "correlationId": "string",
  "robotId": "string",
  "x": "number",
  "y": "number",
  "battery": "number",
  "status": "string",
  "timestamp": "ISO-8601 instant"
}
```

## Field notes

- `eventName`: required, fixed value `robot-state-changed`.
- `correlationId`: required in Phase 2 baseline for traceability.
- `robotId`: stable robot identity.
- `x`, `y`: map-space coordinates.
- `battery`: `0..100` percentage convention.
- `status`: runtime status (e.g., `IDLE`, `MOVING`, `CHARGING`, `ERROR`).
- `timestamp`: producer event time.
- `correlationId` is persisted into `robot_state.correlation_id` for latest-state traceability.

## Compatibility

- Consumers must ignore unknown additive fields.
- Existing required fields must retain semantics in v1.
