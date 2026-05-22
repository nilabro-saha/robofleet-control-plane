package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.robot.application.event.RobotAdvancedEvent;
import com.robofleet.robotsimulator.robot.application.event.RobotCreatedEvent;
import com.robofleet.robotsimulator.robot.application.event.RobotDeletedEvent;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.LifecycleEventType;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotStateChangedEvent;
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
            .correlationId(UUID.randomUUID().toString())
            .robotId(robotView.robotId())
            .positionX(robotView.positionX())
            .positionY(robotView.positionY())
            .battery(robotView.battery())
            .status(robotView.status())
            .timestamp(robotView.timestamp())
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
            .correlationId(UUID.randomUUID().toString())
            .robotId(robotView.robotId())
            .positionX(robotView.positionX())
            .positionY(robotView.positionY())
            .battery(robotView.battery())
            .status(robotView.status())
            .eventType(LifecycleEventType.CREATED)
            .timestamp(robotView.timestamp())
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
            .correlationId(UUID.randomUUID().toString())
            .robotId(robotView.robotId())
            .positionX(robotView.positionX())
            .positionY(robotView.positionY())
            .battery(robotView.battery())
            .status(robotView.status())
            .eventType(LifecycleEventType.REMOVED)
            .timestamp(robotView.timestamp())
            .build());
  }
}
