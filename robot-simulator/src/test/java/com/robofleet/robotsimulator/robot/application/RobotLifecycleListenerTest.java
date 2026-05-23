package com.robofleet.robotsimulator.robot.application;

import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.robofleet.robotsimulator.robot.application.event.RobotAdvancedEvent;
import com.robofleet.robotsimulator.robot.application.event.RobotCreatedEvent;
import com.robofleet.robotsimulator.robot.application.event.RobotDeletedEvent;

import com.robofleet.robotsimulator.robot.domain.model.RobotState;
import com.robofleet.robotsimulator.robot.domain.model.RobotStatus;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotStateChangedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RobotLifecycleListenerTest {

  @Mock
  private RobotLifecyclePublisher robotLifecyclePublisher;

  private RobotState robotView(String robotId) {
    return new RobotState(robotId, 1.0, 2.0, 50.0, RobotStatus.IDLE);
  }

  @Test
  void onRobotAdvanced_shouldPublishTelemetry() {
    RobotLifecycleListener listener = new RobotLifecycleListener(robotLifecyclePublisher);

    listener.onRobotAdvanced(new RobotAdvancedEvent(robotView("robot-1"), "corr-advanced-1"));

    ArgumentCaptor<RobotStateChangedEvent> telemetryCaptor =
        ArgumentCaptor.forClass(RobotStateChangedEvent.class);
    verify(robotLifecyclePublisher).publishTelemetry(telemetryCaptor.capture());
    assertEquals("corr-advanced-1", telemetryCaptor.getValue().getCorrelationId());
  }

  @Test
  void onRobotCreated_shouldPublishLifecycleCreated() {
    RobotLifecycleListener listener = new RobotLifecycleListener(robotLifecyclePublisher);

    listener.onRobotCreated(new RobotCreatedEvent(robotView("robot-1"), "corr-created-1"));

    ArgumentCaptor<RobotLifecycleEvent> lifecycleCaptor =
        ArgumentCaptor.forClass(RobotLifecycleEvent.class);
    verify(robotLifecyclePublisher).publishLifecycle(lifecycleCaptor.capture());
    assertEquals("corr-created-1", lifecycleCaptor.getValue().getCorrelationId());
  }

  @Test
  void onRobotDeleted_shouldPublishLifecycleRemoved() {
    RobotLifecycleListener listener = new RobotLifecycleListener(robotLifecyclePublisher);

    listener.onRobotDeleted(new RobotDeletedEvent(robotView("robot-1"), "corr-deleted-1"));

    ArgumentCaptor<RobotLifecycleEvent> lifecycleCaptor =
        ArgumentCaptor.forClass(RobotLifecycleEvent.class);
    verify(robotLifecyclePublisher).publishLifecycle(lifecycleCaptor.capture());
    assertEquals("corr-deleted-1", lifecycleCaptor.getValue().getCorrelationId());
  }
}
