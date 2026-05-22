# RoboFleet Evolution Roadmap (Detailed Execution Plans)

This directory breaks the high-level roadmap in the root `README.md` into practical,
phase-by-phase execution plans.

## Purpose

- Turn each phase into concrete, actionable workstreams
- Clarify dependencies and acceptance criteria before implementation
- Keep planning docs version-controlled and easy to review

## Phases

1. [Phase 1 — Simulator Runtime Rehydration](./phase-01-simulator-runtime-rehydration.md)
2. [Phase 2 — Foundation Alignment](./phase-02-foundation-alignment.md)
3. [Phase 3 — Task Domain in Control Plane](./phase-03-task-domain-control-plane.md)
4. [Phase 4 — Dashboard Task UX](./phase-04-dashboard-task-ux.md)
5. [Phase 5 — ROS2 Bridge Layer](./phase-05-ros2-bridge-layer.md)
6. [Phase 6 — Gazebo Simulation Execution](./phase-06-gazebo-simulation-execution.md)
7. [Phase 7 — Reliability, Safety, and Scale](./phase-07-reliability-safety-and-scale.md)

## Suggested planning cadence

- Treat each phase plan as input for sprint/iteration breakdown.
- Update the `Current status` section in each phase doc as work lands.
- Add links to merged PRs/issues under each workstream for traceability.

## Checklist update rules (roadmap phase documents)

When updating checklist comments in phase documents (for example under `Suggested deliverables checklist`), use these rules:

- Keep historical comments intact.
  - Do not edit or remove older comment lines unless explicitly requested.
- Do not change timestamps on older comments.
  - Existing timestamps are part of the historical record and must remain unchanged.
- Use `Comment (...)` label consistently.
  - Do not use alternate labels such as `Additional comment (...)`.
- Add a new comment line only when there is a meaningful status delta.
  - If the new note does not add new information beyond an existing comment, do not add it.
- Prefer additive updates over rewrites.
  - Add a new timestamped `Comment (...)` line for new progress while preserving prior context.
