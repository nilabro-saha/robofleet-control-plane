package com.robofleet.robotsimulator.robot.infrastructure.messaging;

import com.robofleet.robotsimulator.robot.application.RobotSimulationOrchestrator;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.LifecycleEventType;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handles DELETE_PENDING lifecycle events by removing robots from simulator runtime.
 */
@Component
@RequiredArgsConstructor
public class DeletePendingLifecycleEventHandler implements RobotLifecycleEventHandler {

  private final RobotSimulationOrchestrator robotSimulationOrchestrator;

  @Override
  public boolean canHandle(RobotLifecycleEvent event) {
    return LifecycleEventType.DELETE_PENDING == event.getEventType();
  }

  @Override
  public void handleEvent(RobotLifecycleEvent event) {
    robotSimulationOrchestrator.deregisterRobot(event.getRobotId());
  }
}
