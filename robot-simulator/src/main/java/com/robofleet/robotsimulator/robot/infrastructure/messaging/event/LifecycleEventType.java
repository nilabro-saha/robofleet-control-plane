package com.robofleet.robotsimulator.robot.infrastructure.messaging.event;

/**
 * Lifecycle event categories shared across robot lifecycle messages.
 *
 * @author Nilabro Saha
 */
public enum LifecycleEventType {
  CREATE_PENDING,
  REHYDRATE_ACTIVE,
  CREATED,
  REMOVED,
  DELETE_PENDING
}
