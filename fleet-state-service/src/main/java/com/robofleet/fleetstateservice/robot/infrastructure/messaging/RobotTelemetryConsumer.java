package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import com.robofleet.fleetstateservice.robot.application.RobotStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka ingestion boundary for robot telemetry.
 *
 * <p>Intent: keep stream consumption simple and resilient—on each message,
 * delegate business handling to the application layer and isolate failures
 * so one bad event does not stop the consumer loop.</p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RobotTelemetryConsumer {

  private final RobotStateService robotStateService;

  /**
   * Consumes one telemetry message from Kafka and forwards it for materialization.
   *
   * @param event telemetry payload deserialized from topic message
   */
  @KafkaListener(
      topics = "${fleet.state.kafka.telemetry-topic:robot.telemetry}",
      groupId = "${spring.kafka.consumer.group-id:fleet-state-service}"
  )
  public void consume(RobotStateChangedEvent event) {
    try {
      robotStateService.upsertFromTelemetry(event);
      log.info("Telemetry consumed for robotId={}", event.getRobotId());
    } catch (Exception e) {
      log.error("Failed to process telemetry event for robotId={} ", event.getRobotId(), e);
    }
  }

  /**
   * Consumes lifecycle events and applies removal commands to materialized state.
   */
  @KafkaListener(
      topics = "${fleet.state.kafka.lifecycle-topic:robot.lifecycle}",
      groupId = "${spring.kafka.consumer.group-id:fleet-state-service}",
      properties = {
          "spring.json.trusted.packages=com.robofleet.fleetstateservice.*",
          "spring.json.value.default.type=${fleet.state.kafka.lifecycle-value-type}",
          "spring.json.use.type.headers=false"
      }
  )
  public void consumeLifecycle(RobotLifecycleEvent event) {
    try {
      if ("REMOVED".equals(event.getEventType())) {
        robotStateService.removeRobotById(event.getRobotId());
        log.info("Lifecycle removal consumed for robotId={}", event.getRobotId());
      }
    } catch (Exception e) {
      log.error("Failed to process lifecycle event for robotId={} ", event.getRobotId(), e);
    }
  }
}
