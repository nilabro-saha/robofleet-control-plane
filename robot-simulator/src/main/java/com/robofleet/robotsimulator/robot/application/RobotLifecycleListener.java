package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.robot.application.event.RobotAdvancedEvent;
import com.robofleet.robotsimulator.robot.application.event.RobotCreatedEvent;
import com.robofleet.robotsimulator.robot.application.event.RobotDeletedEvent;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.LifecycleEventType;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotStateChangedEvent;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens to robot lifecycle/advancement events and publishes to Kafka.
 *
 * <p>Intent: translate internal simulator domain events into external integration events without
 * leaking transport concerns into domain actors.</p>
 *
 * @author Nilabro Saha
 */
@Component
@RequiredArgsConstructor
public class RobotLifecycleListener {

  private static final String TELEMETRY_EVENT_NAME = "robot-state-changed";
  private static final String LIFECYCLE_EVENT_NAME = "robot-lifecycle-changed";

  private final RobotLifecyclePublisher robotLifecyclePublisher;

  /**
   * Handles robot-advanced events by publishing telemetry.
   */
  @EventListener
  public void onRobotAdvanced(RobotAdvancedEvent event) {
    var robotView = event.robotView();
    robotLifecyclePublisher.publishTelemetry(
        RobotStateChangedEvent.builder()
            .eventName(TELEMETRY_EVENT_NAME)
            .correlationId(resolveCorrelationId(event.correlationId()))
            .robotId(robotView.robotId())
            .positionX(robotView.positionX())
            .positionY(robotView.positionY())
            .battery(robotView.battery())
            .status(robotView.status().name())
            .timestamp(Instant.now())
            .build()
    );
  }

  /**
   * Handles robot-created events by publishing lifecycle CREATED.
   */
  @EventListener
  public void onRobotCreated(RobotCreatedEvent event) {
    var robotView = event.robotView();
    robotLifecyclePublisher.publishLifecycle(
        RobotLifecycleEvent.builder()
            .eventName(LIFECYCLE_EVENT_NAME)
            .correlationId(resolveCorrelationId(event.correlationId()))
            .robotId(robotView.robotId())
            .positionX(robotView.positionX())
            .positionY(robotView.positionY())
            .battery(robotView.battery())
            .status(robotView.status().name())
            .eventType(LifecycleEventType.CREATED)
            .timestamp(Instant.now())
            .build());
  }

  /**
   * Handles robot-deleted events by publishing lifecycle REMOVED.
   */
  @EventListener
  public void onRobotDeleted(RobotDeletedEvent event) {
    var robotView = event.robotView();
    robotLifecyclePublisher.publishLifecycle(
        RobotLifecycleEvent.builder()
            .eventName(LIFECYCLE_EVENT_NAME)
            .correlationId(resolveCorrelationId(event.correlationId()))
            .robotId(robotView.robotId())
            .positionX(robotView.positionX())
            .positionY(robotView.positionY())
            .battery(robotView.battery())
            .status(robotView.status().name())
            .eventType(LifecycleEventType.REMOVED)
            .timestamp(Instant.now())
            .build());
  }

  private String resolveCorrelationId(String correlationId) {
    return correlationId != null ? correlationId : UUID.randomUUID().toString();
  }
}
