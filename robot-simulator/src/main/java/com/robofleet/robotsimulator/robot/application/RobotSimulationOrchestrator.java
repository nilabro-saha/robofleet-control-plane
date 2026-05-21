package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.config.SimulatorProperties;
import com.robofleet.robotsimulator.robot.domain.map.MapLocation;
import com.robofleet.robotsimulator.robot.domain.map.RectangularMap;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import com.robofleet.robotsimulator.robot.domain.model.RobotActor;
import com.robofleet.robotsimulator.robot.domain.model.RobotStatus;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Application-level orchestrator for robot lifecycle and simulator scheduling requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RobotSimulationOrchestrator {

  private final SimulatorProperties simulatorProperties;
  private final RobotMap robotMap;
  private final RobotRegistry robotRegistry;

  /**
   * Spawns and registers robots for simulator runtime.
   */
  public void startFleet() {
    var removedRobots = robotRegistry.clearAndGetRemovedRobots();

    for (int i = 1; i <= simulatorProperties.getRobotCount(); i++) {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      MapLocation spawnLocation = robotMap.randomAvailableLocation(random);

      RobotActor robotActor = RobotActor.builder()
          .robotId(UUID.randomUUID().toString())
          .positionX(spawnLocation.x())
          .positionY(spawnLocation.y())
          .battery(random.nextDouble(20.0, 100.0))
          .status(RobotStatus.values()[random.nextInt(RobotStatus.values().length)])
          .build();

      robotRegistry.register(robotActor);

      log.info(
          "Spawned {} at ({}, {}) with battery {} and status {}",
          robotActor.getRobotId(),
          round(robotActor.getPositionX()),
          round(robotActor.getPositionY()),
          round(robotActor.getBattery()),
          robotActor.getStatus().name()
      );
    }

    log.info(
        "Robot simulator started with {} robots (removed {} previous) and telemetry interval {} ms",
        simulatorProperties.getRobotCount(),
        removedRobots.size(),
        simulatorProperties.getTelemetryIntervalMs()
    );
  }

  /**
   * Registers one robot requested externally by control-plane command.
   */
  public void registerRobot(String robotId) {
    registerRobot(robotId, null, null, null, null);
  }

  /**
   * Registers one robot requested externally, preferring provided persisted state when available.
   */
  public void registerRobot(
      String robotId,
      Double positionX,
      Double positionY,
      Double battery,
      String status
  ) {
    boolean alreadyRegistered = robotRegistry.getRegisteredRobots().stream()
        .anyMatch(robot -> robotId.equals(robot.getRobotId()));
    if (alreadyRegistered) {
      log.info("Robot {} already registered, skipping create command", robotId);
      return;
    }

    ThreadLocalRandom random = ThreadLocalRandom.current();

    double resolvedPositionX;
    double resolvedPositionY;
    if (positionX != null && positionY != null) {
      resolvedPositionX = clampX(positionX);
      resolvedPositionY = clampY(positionY);
    } else {
      MapLocation spawnLocation = robotMap.randomAvailableLocation(random);
      resolvedPositionX = spawnLocation.x();
      resolvedPositionY = spawnLocation.y();
    }

    double resolvedBattery = battery == null
        ? random.nextDouble(20.0, 100.0)
        : clampBattery(battery);
    RobotStatus resolvedStatus = resolveStatus(status, random);

    RobotActor robotActor = RobotActor.builder()
        .robotId(robotId)
        .positionX(resolvedPositionX)
        .positionY(resolvedPositionY)
        .battery(resolvedBattery)
        .status(resolvedStatus)
        .build();

    robotRegistry.register(robotActor);
    log.info(
        "Registered command-driven robot {} at ({}, {})",
        robotActor.getRobotId(),
        round(robotActor.getPositionX()),
        round(robotActor.getPositionY())
    );
  }

  /**
   * Deregisters one robot requested by control-plane delete command.
   */
  public void deregisterRobot(String robotId) {
    boolean removed = robotRegistry.deregisterById(robotId);
    if (removed) {
      log.info("Deregistered command-driven robot {}", robotId);
      return;
    }

    log.info("Robot {} not registered, skipping delete command", robotId);
  }

  private double round(double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  private double clampX(double x) {
    if (robotMap instanceof RectangularMap rectangularMap) {
      return rectangularMap.clampX(x);
    }
    return x;
  }

  private double clampY(double y) {
    if (robotMap instanceof RectangularMap rectangularMap) {
      return rectangularMap.clampY(y);
    }
    return y;
  }

  private double clampBattery(double value) {
    return Math.clamp(value, 0.0, 100.0);
  }

  private RobotStatus resolveStatus(String status, ThreadLocalRandom random) {
    if (status == null || status.isBlank()) {
      return RobotStatus.values()[random.nextInt(RobotStatus.values().length)];
    }

    try {
      return RobotStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException exception) {
      log.warn("Unsupported status '{}' in rehydration payload; defaulting to IDLE", status);
      return RobotStatus.IDLE;
    }
  }
}
