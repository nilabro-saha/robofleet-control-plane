package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import com.robofleet.fleetstateservice.robot.application.RobotStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handles REMOVED lifecycle events by removing materialized robot state.
 */
@Component
@RequiredArgsConstructor
public class RemovedLifecycleEventHandler implements RobotLifecycleEventHandler {

  private final RobotStateService robotStateService;

  @Override
  public boolean canHandle(RobotLifecycleEvent event) {
    return LifecycleEventType.REMOVED == event.getEventType();
  }

  @Override
  public void handleEvent(RobotLifecycleEvent event) {
    robotStateService.applyRemovedLifecycleEvent(event);
  }
}
