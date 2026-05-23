package com.robofleet.robotsimulator.robot.infrastructure.messaging;

import com.robofleet.robotsimulator.robot.application.RobotOrchestrator;
import com.robofleet.robotsimulator.robot.application.actor.RobotOrchestration;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.LifecycleEventType;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handles CREATE_PENDING lifecycle events by spawning random-state robots in simulator runtime.
 *
 * <p>Intent: map create lifecycle commands directly into orchestrator spawn commands while keeping
 * command-type branching localized.</p>
 *
 * @author Nilabro Saha
 */
@Component
@RequiredArgsConstructor
public class CreatePendingLifecycleEventHandler implements RobotLifecycleEventHandler {

  private final RobotOrchestrator robotOrchestrator;

  @Override
  public boolean canHandle(RobotLifecycleEvent event) {
    return LifecycleEventType.CREATE_PENDING == event.getEventType();
  }

  @Override
  public void handleEvent(RobotLifecycleEvent event) {
    robotOrchestrator.tell(
        new RobotOrchestration.SpawnRandom(event.getRobotId(), event.getCorrelationId())
    );
  }
}
