# Robot Simulator Module

## Intent

This module is now a **Spring Boot robot actor simulator** for the edge side of RoboFleet.

It models each robot as an in-memory actor with internal mutable state and registers telemetry tasks into a shared Spring-managed thread pool.

## What it does

- Spawns configurable robot actors at startup (default: 3)
- Places all robots on a bounded rectangular map registry
- Initializes each robot with random:
  - location (`x`, `y`)
  - battery charge
  - status (`IDLE`, `MOVING`, `CHARGING`, `ERROR`)
- Runs one telemetry loop per robot using a shared task executor
- Uses a shared scheduled thread pool (no `Thread.sleep`) for periodic telemetry
- Publishes telemetry to Kafka at a configurable interval (default: 1000 ms)

## Instrumentation flow

Each robot actor requests instrumentation itself:

1. Bootstrap spawns/registers robot actor
2. Robot calls `instrument(...)` with a shared `RobotContext`
3. Robot publishes a Spring instrumentation event
4. Event listener registers fixed-rate telemetry schedule for that robot

This keeps telemetry scheduling robot-driven and event-based.

## Runtime contract

- Broker: `localhost:9092`
- Topic: `robot.telemetry`
- Timestamp format: UTC ISO-8601 (Instant compatible)

## Configuration

From `src/main/resources/application.properties`:

- `robot.simulator.robot-count=3`
- `robot.simulator.telemetry-interval-ms=1000`
- `robot.simulator.map-min-x=0`
- `robot.simulator.map-max-x=100`
- `robot.simulator.map-min-y=0`
- `robot.simulator.map-max-y=100`
- `robot.simulator.kafka.topic=robot.telemetry`

## Run

```bash
cd robot-simulator
mvn spring-boot:run
```
