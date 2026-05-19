# Robot Simulator Module

## Intent

This module represents the **edge side** of the RoboFleet story.

Its purpose is not to be physically accurate, but to make the downstream control-plane behavior observable:

- each robot emits changing position/battery/status,
- telemetry is produced continuously,
- backend and UI can be exercised exactly like a real fleet stream.

## Why this exists in the MVP

Without a simulator, every backend/UI iteration depends on real hardware or external data feeds.
This module gives a deterministic, always-available signal source so development can move fast.

## Main artifact

- `src/robot_simulator.py`: starts three virtual robots and publishes telemetry to `robot.telemetry` every second.

## Runtime contract

- Broker: `localhost:9092`
- Topic: `robot.telemetry`
- Timestamp format: UTC ISO-8601 (Instant compatible, `Z` suffix)
