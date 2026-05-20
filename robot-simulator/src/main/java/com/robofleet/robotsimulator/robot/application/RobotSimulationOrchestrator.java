package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.config.SimulatorProperties;
import com.robofleet.robotsimulator.robot.domain.RobotActor;
import com.robofleet.robotsimulator.robot.domain.RobotStatus;
import com.robofleet.robotsimulator.robot.domain.map.MapLocation;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
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

  private double round(double value) {
    return Math.round(value * 100.0) / 100.0;
  }
}
