# Draft Contract: `robot.task.status` (v1)

## Topic

- `robot.task.status`

## Purpose

Future status channel to report task execution progress/results back to control-plane.

## JSON schema (draft)

```json
{
  "correlationId": "string",
  "taskId": "string",
  "robotId": "string",
  "status": "string",
  "progress": "number",
  "message": "string",
  "timestamp": "ISO-8601 instant"
}
```

## Notes

- Phase 2 status: draft only (topic not implemented in code yet).
- `status` examples: `QUEUED`, `ASSIGNED`, `RUNNING`, `COMPLETED`, `FAILED`.
- `correlationId` links status stream to originating task command.
