package com.robofleet.robotsimulator.robot.infrastructure.messaging;

import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka lifecycle consumer for simulator-side lifecycle handling.
 *
 * <p>Intent: serve as the inbound messaging boundary that decodes lifecycle events and delegates
 * business handling to dedicated strategy handlers.</p>
 *
 * @author Nilabro Saha
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RobotLifecycleConsumer {

  private final List<RobotLifecycleEventHandler> lifecycleEventHandlers;

  /**
   * Consumes one lifecycle event and dispatches it to matching handlers.
   */
  @KafkaListener(
      topics = "${robot.simulator.kafka.lifecycle-topic:robot.lifecycle}",
      groupId = "${spring.kafka.consumer.group-id:robot-simulator}"
  )
  public void consume(RobotLifecycleEvent event) {
    try {
      lifecycleEventHandlers.stream()
          .filter(handler -> handler.canHandle(event))
          .forEach(handler -> handler.handleEvent(event));
      log.info("Lifecycle {} consumed for robotId={}", event.getEventType(), event.getRobotId());
    } catch (Exception e) {
      log.error("Failed to consume lifecycle event for robotId={}", event.getRobotId(), e);
    }
  }
}
