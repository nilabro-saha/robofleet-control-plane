package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.config.SimulatorProperties;
import com.robofleet.robotsimulator.robot.domain.RobotActor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens for state advancement requests and registers random movement/state evolution tasks.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RobotStateAdvancementListener {

  private final ScheduledExecutorService robotTelemetryScheduler;
  private final SimulatorProperties simulatorProperties;
  private final RobotContext robotContext;

  /**
   * Registers fixed-rate state advancement for the requested robot actor.
   */
  @EventListener
  public void onRobotStateAdvancementRequested(RobotStateAdvancementRequestedEvent event) {
    RobotActor robotActor = event.robotActor();

    robotTelemetryScheduler.scheduleAtFixedRate(
        () -> advanceState(robotActor),
        0,
        simulatorProperties.getStateAdvanceIntervalMs(),
        TimeUnit.MILLISECONDS
    );

    log.info(
        "Registered state advancement schedule for {} (advance={}ms)",
        robotActor.getRobotId(),
        simulatorProperties.getStateAdvanceIntervalMs()
    );
  }

  private void advanceState(RobotActor robotActor) {
    try {
      robotActor.advanceState(robotContext);
    } catch (Exception exception) {
      log.error("Failed to advance state for {}", robotActor.getRobotId(), exception);
    }
  }
}
