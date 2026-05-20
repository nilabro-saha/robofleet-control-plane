package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.config.SimulatorProperties;
import com.robofleet.robotsimulator.robot.domain.RobotActor;
import com.robofleet.robotsimulator.robot.domain.RobotStatus;
import com.robofleet.robotsimulator.robot.domain.map.MapLocation;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Spawns initial robots, registers them, and schedules periodic telemetry tasks.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RobotFleetBootstrap {

  private final SimulatorProperties simulatorProperties;
  private final RobotMap robotMap;
  private final RobotContext robotContext;

  /**
   * Initializes default robot fleet and starts publish loops.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void bootstrapFleet() {
    for (int i = 1; i <= simulatorProperties.getRobotCount(); i++) {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      MapLocation spawnLocation = robotMap.randomAvailableLocation(random);
      RobotActor robotActor = RobotActor.builder()
          .robotId("robot-" + i)
          .x(spawnLocation.x())
          .y(spawnLocation.y())
          .battery(random.nextDouble(20.0, 100.0))
          .status(RobotStatus.values()[random.nextInt(RobotStatus.values().length)])
          .build();

      robotActor.registerWith(robotContext);
      robotActor.instrument(robotContext);
      robotActor.scheduleRandomAdvancement(robotContext);

      log.info(
          "Spawned {} at ({}, {}) with battery {} and status {}",
          robotActor.getRobotId(),
          round(robotActor.getX()),
          round(robotActor.getY()),
          round(robotActor.getBattery()),
          robotActor.getStatus().name()
      );
    }

    log.info(
        "Robot simulator started with {} robots and telemetry interval {} ms",
        simulatorProperties.getRobotCount(),
        simulatorProperties.getTelemetryIntervalMs()
    );
  }

  private double round(double value) {
    return Math.round(value * 100.0) / 100.0;
  }
}
