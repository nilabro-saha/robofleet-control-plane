package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.robot.application.event.RobotAdvancedEvent;
import com.robofleet.robotsimulator.robot.application.event.RobotCreatedEvent;
import com.robofleet.robotsimulator.robot.application.event.RobotDeletedEvent;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.LifecycleEventType;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotStateChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens to robot lifecycle/advancement events and publishes to Kafka.
 */
@Component
@RequiredArgsConstructor
public class RobotLifecycleListener {

  private final RobotLifecyclePublisher robotLifecyclePublisher;

  /**
   * Handles robot-advanced events by publishing telemetry.
   */
  @EventListener
  public void onRobotAdvanced(RobotAdvancedEvent event) {
    var lastStateView = event.robotActor().lastStateView();
    robotLifecyclePublisher.publishTelemetry(
        RobotStateChangedEvent.builder()
            .robotId(lastStateView.robotId())
            .positionX(lastStateView.positionX())
            .positionY(lastStateView.positionY())
            .battery(lastStateView.battery())
            .status(lastStateView.status())
            .timestamp(lastStateView.timestamp())
            .build()
    );
  }

  /**
   * Handles robot-created events by publishing lifecycle CREATED.
   */
  @EventListener
  public void onRobotCreated(RobotCreatedEvent event) {
    var lastStateView = event.robotActor().lastStateView();
    robotLifecyclePublisher.publishLifecycle(
        RobotLifecycleEvent.builder()
            .robotId(lastStateView.robotId())
            .positionX(lastStateView.positionX())
            .positionY(lastStateView.positionY())
            .battery(lastStateView.battery())
            .status(lastStateView.status())
            .eventType(LifecycleEventType.CREATED)
            .timestamp(lastStateView.timestamp())
            .build());
  }

  /**
   * Handles robot-deleted events by publishing lifecycle REMOVED.
   */
  @EventListener
  public void onRobotDeleted(RobotDeletedEvent event) {
    var lastStateView = event.robotActor().lastStateView();
    robotLifecyclePublisher.publishLifecycle(
        RobotLifecycleEvent.builder()
            .robotId(lastStateView.robotId())
            .positionX(lastStateView.positionX())
            .positionY(lastStateView.positionY())
            .battery(lastStateView.battery())
            .status(lastStateView.status())
            .eventType(LifecycleEventType.REMOVED)
            .timestamp(lastStateView.timestamp())
            .build());
  }
}
