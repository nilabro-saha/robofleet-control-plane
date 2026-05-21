package com.robofleet.robotsimulator.robot.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Canonical schema versions for robot-simulator event payloads.
 */
@Getter
@RequiredArgsConstructor
public enum SchemaVersion {
  V1("v1");

  @JsonValue
  private final String value;
}
