package com.robofleet.robotsimulator.robot.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Lifecycle contract emitted to Kafka for create/remove state changes.
 */
@Value
@Builder
@Jacksonized
public class RobotLifecycleEvent implements KafkaEventMetadata {
  String eventName;
  String correlationId;
  String robotId;
  @JsonProperty("x")
  Double positionX;
  @JsonProperty("y")
  Double positionY;
  Double battery;
  String status;
  LifecycleEventType eventType;
  Instant timestamp;

  @Override
  @JsonProperty("schemaVersion")
  public SchemaVersion getSchemaVersion() {
    return SchemaVersion.V1;
  }
}
