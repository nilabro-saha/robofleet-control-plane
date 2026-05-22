package com.robofleet.fleetstateservice.robot.application;

import com.robofleet.fleetstateservice.robot.infrastructure.messaging.RobotLifecycleEvent;

/**
 * Outbound port for publishing robot lifecycle events.
 *
 * @author Nilabro Saha
 */
public interface RobotCreationCommandGateway {

  /**
   * Publishes one lifecycle event to downstream consumers.
   *
   * @param command lifecycle command payload to publish
   */
  void publishLifecycleEvent(RobotLifecycleEvent command);
}
