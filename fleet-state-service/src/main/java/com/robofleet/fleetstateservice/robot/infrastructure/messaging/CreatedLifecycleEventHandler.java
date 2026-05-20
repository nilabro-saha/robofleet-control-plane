package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import com.robofleet.fleetstateservice.robot.application.RobotStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handles CREATED lifecycle events by applying activation materialization.
 */
@Component
@RequiredArgsConstructor
public class CreatedLifecycleEventHandler implements RobotLifecycleEventHandler {

  private final RobotStateService robotStateService;

  @Override
  public boolean canHandle(RobotLifecycleEvent event) {
    return LifecycleEventType.CREATED == event.getEventType();
  }

  @Override
  public void handleEvent(RobotLifecycleEvent event) {
    robotStateService.applyCreatedLifecycleEvent(event);
  }
}
