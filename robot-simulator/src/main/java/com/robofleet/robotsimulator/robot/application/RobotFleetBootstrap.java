package com.robofleet.robotsimulator.robot.application;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Spawns initial robots, registers them, and schedules periodic telemetry tasks.
 */
@Component
@RequiredArgsConstructor
public class RobotFleetBootstrap {

  private final RobotSimulationOrchestrator robotSimulationOrchestrator;

  /**
   * Initializes default robot fleet and starts publish loops.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void bootstrapFleet() {
    robotSimulationOrchestrator.startFleet();
  }
}
