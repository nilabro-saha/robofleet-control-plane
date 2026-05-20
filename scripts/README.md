# Scripts Module

## Intent

These scripts are the **operator convenience layer** for the MVP.

They reduce startup friction so the whole system can be launched/stopped predictably without memorizing many commands.

## Why this exists in the MVP

An MVP is most valuable when anyone can run it quickly.
These scripts make the demo reproducible across different machine constraints (Docker present vs absent, port conflicts, existing host Kafka, etc.).

## Responsibilities

- `mvp-up.sh`
  - starts (or reuses) Kafka
  - starts fleet-state-service
  - starts Spring robot-simulator service
  - starts dashboard UI static server
  - records PIDs/ports/logs under `.run/`

- `mvp-down.sh`
  - stops all processes started by `mvp-up.sh`
  - cleans PID/port bookkeeping files

- `kafka-local-up.sh` / `kafka-local-down.sh`
  - fallback Kafka lifecycle for environments without Docker

## Design note

These scripts prioritize operational clarity over cleverness:

- explicit log files
- explicit PID files
- deterministic messages about what was started and where
