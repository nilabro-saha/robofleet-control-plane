package com.robofleet.robotsimulator.robot.application;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Spawns initial robots, registers them, and schedules periodic telemetry tasks.
 *
 * <p>Intent: provide a single startup hook that initializes simulator fleet state after the Spring
 * context is fully ready.</p>
 *
 * @author Nilabro Saha
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
