package com.robofleet.robotsimulator.robot.infrastructure.messaging;

import com.robofleet.robotsimulator.robot.application.RobotOrchestrator;
import com.robofleet.robotsimulator.robot.application.actor.RobotOrchestration;
import com.robofleet.robotsimulator.robot.domain.model.RobotState;
import com.robofleet.robotsimulator.robot.domain.model.RobotStatus;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.LifecycleEventType;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handles REHYDRATE_ACTIVE lifecycle events by spawning fixed-state robots in simulator runtime.
 *
 * <p>Intent: map rehydrate lifecycle commands directly into orchestrator fixed-state spawn commands
 * while keeping command-type branching localized.</p>
 *
 * @author Nilabro Saha
 */
@Component
@RequiredArgsConstructor
public class RehydrateActiveLifecycleEventHandler implements RobotLifecycleEventHandler {

  private final RobotOrchestrator robotOrchestrator;

  @Override
  public boolean canHandle(RobotLifecycleEvent event) {
    return LifecycleEventType.REHYDRATE_ACTIVE == event.getEventType();
  }

  @Override
  public void handleEvent(RobotLifecycleEvent event) {
    RobotStatus resolvedStatus;
    try {
      resolvedStatus = RobotStatus.valueOf(event.getStatus().toUpperCase());
    } catch (Exception exception) {
      resolvedStatus = RobotStatus.IDLE;
    }

    RobotState fixedState = new RobotState(
        event.getRobotId(),
        event.getPositionX() != null ? event.getPositionX() : 0.0,
        event.getPositionY() != null ? event.getPositionY() : 0.0,
        event.getBattery() != null ? event.getBattery() : 100.0,
        resolvedStatus
    );
    robotOrchestrator.tell(new RobotOrchestration.Spawn(fixedState, event.getCorrelationId()));
  }
}