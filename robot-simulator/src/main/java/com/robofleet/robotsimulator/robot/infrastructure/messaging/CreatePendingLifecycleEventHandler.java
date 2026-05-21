package com.robofleet.robotsimulator.robot.infrastructure.messaging;

import com.robofleet.robotsimulator.robot.application.RobotSimulationOrchestrator;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.LifecycleEventType;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handles CREATE_PENDING lifecycle events by registering robots in simulator runtime.
 */
@Component
@RequiredArgsConstructor
public class CreatePendingLifecycleEventHandler implements RobotLifecycleEventHandler {

  private final RobotSimulationOrchestrator robotSimulationOrchestrator;

  @Override
  public boolean canHandle(RobotLifecycleEvent event) {
    return LifecycleEventType.CREATE_PENDING == event.getEventType()
        || LifecycleEventType.REHYDRATE_ACTIVE == event.getEventType();
  }

  @Override
  public void handleEvent(RobotLifecycleEvent event) {
    robotSimulationOrchestrator.registerRobot(event.getRobotId());
  }
}
