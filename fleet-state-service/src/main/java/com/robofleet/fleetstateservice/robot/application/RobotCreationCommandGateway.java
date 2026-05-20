package com.robofleet.fleetstateservice.robot.application;

import com.robofleet.fleetstateservice.robot.infrastructure.messaging.RobotLifecycleEvent;

/**
 * Outbound port for publishing robot lifecycle events.
 */
public interface RobotCreationCommandGateway {

  /**
   * Publishes one lifecycle event to downstream consumers.
   */
  void publishLifecycleEvent(RobotLifecycleEvent command);
}
