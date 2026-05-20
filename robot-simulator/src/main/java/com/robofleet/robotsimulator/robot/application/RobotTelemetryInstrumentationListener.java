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
 * Listens for robot instrumentation requests and registers scheduled telemetry tasks.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RobotTelemetryInstrumentationListener {

  private final ScheduledExecutorService robotTelemetryScheduler;
  private final SimulatorProperties simulatorProperties;
  private final RobotTelemetryPublisher robotTelemetryPublisher;

  /**
   * Registers fixed-rate telemetry publishing for the requested robot actor.
   */
  @EventListener
  public void onRobotTelemetryInstrumentationRequested(
      RobotTelemetryInstrumentationRequestedEvent event) {
    RobotActor robotActor = event.robotActor();

    robotTelemetryScheduler.scheduleAtFixedRate(
        () -> publishCurrentState(robotActor),
        0,
        simulatorProperties.getTelemetryIntervalMs(),
        TimeUnit.MILLISECONDS
    );

    log.info(
        "Registered telemetry schedule for {} (publish={}ms)",
        robotActor.getRobotId(),
        simulatorProperties.getTelemetryIntervalMs()
    );
  }

  private void publishCurrentState(RobotActor robotActor) {
    try {
      robotTelemetryPublisher.publish(robotActor.currentTelemetry());
    } catch (Exception exception) {
      log.error("Failed to publish telemetry for {}", robotActor.getRobotId(), exception);
    }
  }

}
