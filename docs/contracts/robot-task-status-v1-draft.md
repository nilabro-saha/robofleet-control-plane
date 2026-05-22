# Draft Contract: `robot.task.status` (v1)

## Topic

- `robot.task.status`

## Purpose

Planned status channel for reporting task progress and outcomes back to the control plane.

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

- Phase 2 status: draft only (topic is not implemented yet).
- `status` examples: `QUEUED`, `ASSIGNED`, `RUNNING`, `COMPLETED`, `FAILED`.
- `correlationId` links each status event to the original task command.
