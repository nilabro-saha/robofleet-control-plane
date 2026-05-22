package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

/**
 * Strategy contract for handling one lifecycle event category.
 *
 * @author Nilabro Saha
 */
public interface RobotLifecycleEventHandler {

  /**
   * Returns true when this handler should process the event.
   *
   * @param event lifecycle event candidate
   * @return true when this handler supports the event type
   */
  boolean canHandle(RobotLifecycleEvent event);

  /**
   * Processes the event.
   *
   * @param event lifecycle event payload
   */
  void handleEvent(RobotLifecycleEvent event);
}
