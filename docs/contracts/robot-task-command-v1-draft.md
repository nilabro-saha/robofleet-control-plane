# Draft Contract: `robot.task.command` (v1)

## Topic

- `robot.task.command`

## Purpose

Future control-plane command channel to request robot task execution.

## JSON schema (draft)

```json
{
  "correlationId": "string",
  "taskId": "string",
  "robotId": "string",
  "taskType": "string",
  "pickup": { "x": "number", "y": "number", "z": "number", "frame": "string" },
  "dropoff": { "x": "number", "y": "number", "z": "number", "frame": "string" },
  "priority": "string",
  "createdAt": "ISO-8601 instant"
}
```

## Notes

- Phase 2 status: draft only (topic not implemented in code yet).
- `correlationId` will be mandatory from first implementation.
