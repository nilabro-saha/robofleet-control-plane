package com.robofleet.robotsimulator.robot.infrastructure.messaging;

import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;

/**
 * Strategy contract for handling one lifecycle event category.
 */
public interface RobotLifecycleEventHandler {

  /**
   * Returns true when this handler should process the event.
   */
  boolean canHandle(RobotLifecycleEvent event);

  /**
   * Processes the event.
   */
  void handleEvent(RobotLifecycleEvent event);
}
