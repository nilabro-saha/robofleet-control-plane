package com.robofleet.robotsimulator.robot.infrastructure.messaging;

import com.robofleet.robotsimulator.robot.application.RobotOrchestrator;
import com.robofleet.robotsimulator.robot.application.actor.RobotOrchestration;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.LifecycleEventType;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handles DELETE_PENDING lifecycle events by removing robots from simulator runtime.
 *
 * <p>Intent: localize delete-command handling so simulator de-registration behavior remains
 * independent from Kafka consumer wiring.</p>
 *
 * @author Nilabro Saha
 */
@Component
@RequiredArgsConstructor
public class DeletePendingLifecycleEventHandler implements RobotLifecycleEventHandler {

  private final RobotOrchestrator robotOrchestrator;

  @Override
  public boolean canHandle(RobotLifecycleEvent event) {
    return LifecycleEventType.DELETE_PENDING == event.getEventType();
  }

  @Override
  public void handleEvent(RobotLifecycleEvent event) {
    robotOrchestrator.tell(
        new RobotOrchestration.Destroy(event.getRobotId(), event.getCorrelationId())
    );
  }
}
