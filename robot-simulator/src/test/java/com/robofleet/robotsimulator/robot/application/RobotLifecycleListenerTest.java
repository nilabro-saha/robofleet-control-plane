package com.robofleet.robotsimulator.robot.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import com.robofleet.robotsimulator.robot.application.event.RobotAdvancedEvent;
import com.robofleet.robotsimulator.robot.application.event.RobotCreatedEvent;
import com.robofleet.robotsimulator.robot.application.event.RobotDeletedEvent;

import com.robofleet.robotsimulator.robot.domain.model.RobotState;
import com.robofleet.robotsimulator.robot.domain.model.RobotStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    listener.onRobotAdvanced(new RobotAdvancedEvent(robotView("robot-1")));

    verify(robotLifecyclePublisher).publishTelemetry(any());
  }

  @Test
  void onRobotCreated_shouldPublishLifecycleCreated() {
    RobotLifecycleListener listener = new RobotLifecycleListener(robotLifecyclePublisher);

    listener.onRobotCreated(new RobotCreatedEvent(robotView("robot-1")));

    verify(robotLifecyclePublisher).publishLifecycle(any());
  }

  @Test
  void onRobotDeleted_shouldPublishLifecycleRemoved() {
    RobotLifecycleListener listener = new RobotLifecycleListener(robotLifecyclePublisher);

    listener.onRobotDeleted(new RobotDeletedEvent(robotView("robot-1")));

    verify(robotLifecyclePublisher).publishLifecycle(any());
  }
}
