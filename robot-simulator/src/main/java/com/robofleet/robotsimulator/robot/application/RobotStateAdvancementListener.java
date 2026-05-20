package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.robot.domain.AdvancementMode;
import com.robofleet.robotsimulator.robot.domain.RobotActor;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens for single state advancement requests and publishes telemetry for advanced state.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RobotStateAdvancementListener {

  private final RobotMap robotMap;
  private final ApplicationEventPublisher applicationEventPublisher;

  /**
   * Advances state once and immediately publishes telemetry for updated state.
   */
  @EventListener
  public void onRobotStateAdvancementRequested(AdvanceRobotStateRequest event) {
    RobotActor robotActor = event.robotActor();
    advanceStateAndPublishTelemetry(robotActor, event.advancementMode());
  }

  private void advanceStateAndPublishTelemetry(
      RobotActor robotActor,
      AdvancementMode advancementMode) {
    try {
      robotActor.advanceState(robotMap, advancementMode);
      applicationEventPublisher.publishEvent(new RobotAdvancedEvent(robotActor));
    } catch (Exception exception) {
      log.error("Failed to advance/publish state for {}", robotActor.getRobotId(), exception);
    }
  }
}
