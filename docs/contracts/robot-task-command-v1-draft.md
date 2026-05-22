# Draft Contract: `robot.task.command` (v1)

## Topic

- `robot.task.command`

## Purpose

Planned control-plane command channel for requesting robot task execution.

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

- Phase 2 status: draft only (topic is not implemented yet).
- `correlationId` will be required from the first implementation.
