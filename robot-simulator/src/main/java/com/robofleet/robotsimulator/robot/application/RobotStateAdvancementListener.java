package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.robot.application.command.AdvanceRobotStateRequest;
import com.robofleet.robotsimulator.robot.application.event.RobotAdvancedEvent;
import com.robofleet.robotsimulator.robot.domain.behavior.AdvancementMode;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import com.robofleet.robotsimulator.robot.domain.model.RobotActor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens for single state advancement requests and publishes telemetry for advanced state.
 *
 * <p>Intent: isolate advancement execution and failure handling from scheduler/event wiring so
 * domain state transitions remain predictable and observable.</p>
 *
 * @author Nilabro Saha
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
