package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Canonical schema versions for fleet-state-service event payloads.
 *
 * @author Nilabro Saha
 */
@Getter
@RequiredArgsConstructor
public enum SchemaVersion {
  V1("v1");

  @JsonValue
  private final String value;
}
