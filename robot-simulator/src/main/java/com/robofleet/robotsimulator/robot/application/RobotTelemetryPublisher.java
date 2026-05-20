package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.robot.infrastructure.messaging.RobotTelemetryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Emits robot telemetry events to Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RobotTelemetryPublisher {

  private final KafkaTemplate<String, RobotTelemetryEvent> kafkaTemplate;

  @Value("${robot.simulator.kafka.topic:robot.telemetry}")
  private String telemetryTopic;

  /**
   * Publishes one telemetry event keyed by robot id.
   */
  public void publish(RobotTelemetryEvent telemetryEvent) {
    kafkaTemplate.send(telemetryTopic, telemetryEvent.getRobotId(), telemetryEvent);
    log.debug("Published telemetry for {}", telemetryEvent.getRobotId());
  }
}
