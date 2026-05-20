package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import com.robofleet.fleetstateservice.robot.application.RobotStateService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka ingestion boundary for robot telemetry and lifecycle events.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RobotLifecycleConsumer {

  private final RobotStateService robotStateService;
  private final List<RobotLifecycleEventHandler> lifecycleEventHandlers;

  /**
   * Consumes one telemetry message from Kafka and forwards it for materialization.
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
   * Consumes lifecycle events and dispatches to matching handlers.
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
      lifecycleEventHandlers.stream()
          .filter(handler -> handler.canHandle(event))
          .forEach(handler -> handler.handleEvent(event));
      log.info("Lifecycle {} consumed for robotId={}", event.getEventType(), event.getRobotId());
    } catch (Exception e) {
      log.error("Failed to process lifecycle event for robotId={} ", event.getRobotId(), e);
    }
  }
}
