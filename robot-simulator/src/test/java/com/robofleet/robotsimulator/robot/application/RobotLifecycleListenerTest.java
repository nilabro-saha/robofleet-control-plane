package com.robofleet.robotsimulator.robot.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import com.robofleet.robotsimulator.robot.application.event.RobotAdvancedEvent;
import com.robofleet.robotsimulator.robot.application.event.RobotCreatedEvent;
import com.robofleet.robotsimulator.robot.application.event.RobotDeletedEvent;

import com.robofleet.robotsimulator.robot.domain.model.RobotActor;
import com.robofleet.robotsimulator.robot.domain.model.RobotStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RobotLifecycleListenerTest {

  @Mock
  private RobotLifecyclePublisher robotLifecyclePublisher;

  @Test
  void onRobotAdvanced_shouldPublishTelemetry() {
    RobotLifecycleListener listener = new RobotLifecycleListener(robotLifecyclePublisher);
    RobotActor robotActor = RobotActor.builder()
        .robotId("robot-1")
        .displayName("Alpha")
        .positionX(1.0)
        .positionY(2.0)
        .battery(50.0)
        .status(RobotStatus.IDLE)
        .build();

    listener.onRobotAdvanced(new RobotAdvancedEvent(robotActor));

    verify(robotLifecyclePublisher).publishTelemetry(any());
  }

  @Test
  void onRobotCreated_shouldPublishLifecycleCreated() {
    RobotLifecycleListener listener = new RobotLifecycleListener(robotLifecyclePublisher);
    RobotActor robotActor = RobotActor.builder()
        .robotId("robot-1")
        .displayName(null)
        .positionX(1.0)
        .positionY(2.0)
        .battery(50.0)
        .status(RobotStatus.IDLE)
        .build();

    listener.onRobotCreated(new RobotCreatedEvent(robotActor));

    verify(robotLifecyclePublisher).publishLifecycle(any());
  }

  @Test
  void onRobotDeleted_shouldPublishLifecycleRemoved() {
    RobotLifecycleListener listener = new RobotLifecycleListener(robotLifecyclePublisher);
    RobotActor robotActor = RobotActor.builder()
        .robotId("robot-1")
        .displayName("Gamma")
        .positionX(1.0)
        .positionY(2.0)
        .battery(50.0)
        .status(RobotStatus.IDLE)
        .build();

    listener.onRobotDeleted(new RobotDeletedEvent(robotActor));

    verify(robotLifecyclePublisher).publishLifecycle(any());
  }
}
