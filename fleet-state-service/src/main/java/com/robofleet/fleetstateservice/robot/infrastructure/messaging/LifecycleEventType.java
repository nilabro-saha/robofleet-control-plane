package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

/**
 * Lifecycle event categories shared across robot lifecycle messages.
 */
public enum LifecycleEventType {
  CREATE_PENDING,
  CREATED,
  REMOVED,
  DELETE_PENDING
}
