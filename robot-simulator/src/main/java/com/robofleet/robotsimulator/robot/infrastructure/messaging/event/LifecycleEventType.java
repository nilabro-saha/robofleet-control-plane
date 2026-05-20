package com.robofleet.robotsimulator.robot.infrastructure.messaging.event;

/**
 * Lifecycle event categories shared across robot lifecycle messages.
 */
public enum LifecycleEventType {
  CREATE_PENDING,
  CREATED,
  REMOVED,
  DELETE_PENDING
}
