package com.robofleet.robotsimulator.robot.infrastructure.messaging;

import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;

/**
 * Strategy contract for handling one lifecycle event category.
 *
 * <p>Intent: keep lifecycle-event branching open for extension via small focused handlers instead
 * of hard-coding conditional logic in the Kafka consumer.</p>
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
