package com.robofleet.robotsimulator.robot.infrastructure.messaging.event;

import java.time.Instant;

/**
 * Shared metadata contract for Kafka events in robot-simulator.
 *
 * @author Nilabro Saha
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
