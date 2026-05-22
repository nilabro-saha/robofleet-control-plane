package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

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
