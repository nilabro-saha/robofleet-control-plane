package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotStateChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Emits simulator telemetry and lifecycle events to Kafka topics.
 *
 * <p>Intent: keep Kafka publishing concerns isolated from domain/application event handling so
 * lifecycle listeners remain transport-agnostic.</p>
 *
 * @author Nilabro Saha
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RobotLifecyclePublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Value("${robot.simulator.kafka.topic:robot.telemetry}")
  private String telemetryTopic;

  @Value("${robot.simulator.kafka.lifecycle-topic:robot.lifecycle}")
  private String lifecycleTopic;

  /**
   * Publishes one telemetry event keyed by robot id.
   *
   * @param telemetryEvent telemetry payload to publish
   */
  public void publishTelemetry(RobotStateChangedEvent telemetryEvent) {
    kafkaTemplate.send(telemetryTopic, telemetryEvent.getRobotId(), telemetryEvent);
    log.debug("Published telemetry for {}", telemetryEvent.getRobotId());
  }

  /**
   * Publishes one lifecycle event keyed by robot id.
   *
   * @param lifecycleEvent lifecycle payload to publish
   */
  public void publishLifecycle(RobotLifecycleEvent lifecycleEvent) {
    kafkaTemplate.send(lifecycleTopic, lifecycleEvent.getRobotId(), lifecycleEvent);
    log.debug(
        "Published lifecycle {} for {}",
        lifecycleEvent.getEventType(),
        lifecycleEvent.getRobotId()
    );
  }
}
