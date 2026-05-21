# Phase 4 — Dashboard Task UX

## Objective

Enable operators to create tasks and monitor progress from the dashboard without needing direct API tools.

## Scope

- Task creation interface
- Task list + filtering
- Task details/timeline panel
- Robot-task relationship visibility

## UX goals

- A new operator can create a task in under 30 seconds.
- Task status changes are visible within polling interval.
- Failures provide clear, actionable reason text.

## Planned features

1. Task creation form
   - Inputs: task type, pickup coordinates, drop coordinates, optional priority
   - Client-side validation for required fields and coordinate ranges
2. Task board/list
   - Status chips: `QUEUED`, `ASSIGNED`, `RUNNING`, `COMPLETED`, `FAILED`, `CANCELLED`
   - Filters by status, robot, and time window
3. Task detail drawer
   - Timeline of lifecycle events
   - Assigned robot and latest progress message
   - Failure/cancellation context
4. Robot-task linking
   - Show active task against selected robot row/detail panel

## Technical plan

- Extend `fleet-dashboard-ui/src/app.js` with task query and create flows.
- Add task sections in `index.html` and corresponding CSS states in `styles.css`.
- Reuse existing API base query param mechanism (`?apiBase=`).
- Keep server-side sorting/filtering where available; avoid heavy client-side joins.

## Testing plan

- Unit-style tests for state formatting helpers (if UI test harness is introduced).
- Manual scenario scripts:
  - create task -> observe queued
  - assign/run/completed progression
  - failed task with reason

## Exit criteria

- Task creation and monitoring are fully usable from browser.
- Operator can navigate from robot to active task context quickly.
