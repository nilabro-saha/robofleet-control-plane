package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import java.time.Instant;

/**
 * Shared metadata contract for Kafka events in fleet-state-service.
 */
public interface KafkaEventMetadata {

  /**
   * Returns the canonical kebab-case event name used to identify the payload kind.
   */
  String getEventName();

  /**
   * Returns the correlation identifier used to trace related events end-to-end.
   */
  String getCorrelationId();

  /**
   * Returns the producer event timestamp.
   */
  Instant getTimestamp();

  /**
   * Returns the payload schema version for compatibility handling.
   */
  SchemaVersion getSchemaVersion();
}
