package com.robofleet.robotsimulator.robot.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.same;

import com.robofleet.robotsimulator.robot.application.command.AdvanceRobotStateRequest;
import com.robofleet.robotsimulator.robot.application.event.RobotAdvancedEvent;
import com.robofleet.robotsimulator.robot.domain.behavior.AdvancementMode;
import com.robofleet.robotsimulator.robot.domain.behavior.RandomAdvance;
import com.robofleet.robotsimulator.robot.domain.model.RobotActor;
import com.robofleet.robotsimulator.robot.domain.model.RobotStatus;
import com.robofleet.robotsimulator.robot.domain.map.RectangularMap;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class RobotStateAdvancementListenerTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @Test
  void onRobotStateAdvancementRequested_shouldAdvanceAndPublishTelemetry() {
    RobotMap robotMap = new RectangularMap(0.0, 100.0, 0.0, 100.0);
    RobotStateAdvancementListener listener = new RobotStateAdvancementListener(
        robotMap,
        applicationEventPublisher);

    RobotActor robotActor = spy(RobotActor.builder()
        .robotId("robot-1")
        .positionX(10.0)
        .positionY(10.0)
        .battery(60.0)
        .status(RobotStatus.IDLE)
        .build());
    AdvancementMode advancementMode = new RandomAdvance(ThreadLocalRandom.current());
    doNothing().when(robotActor).advanceState(any(), any());

    listener.onRobotStateAdvancementRequested(new AdvanceRobotStateRequest(robotActor, advancementMode));

    verify(robotActor).advanceState(same(robotMap), same(advancementMode));
    verify(applicationEventPublisher).publishEvent(any(RobotAdvancedEvent.class));
  }

  @Test
  void onRobotStateAdvancementRequested_shouldSwallowErrors() {
    RobotMap robotMap = new RectangularMap(0.0, 100.0, 0.0, 100.0);
    RobotStateAdvancementListener listener = new RobotStateAdvancementListener(
        robotMap,
        applicationEventPublisher);

    RobotActor robotActor = spy(RobotActor.builder()
        .robotId("robot-2")
        .positionX(90.0)
        .positionY(90.0)
        .battery(40.0)
        .status(RobotStatus.MOVING)
        .build());
    AdvancementMode randomAdvance = new RandomAdvance(ThreadLocalRandom.current());
    doThrow(new RuntimeException("boom")).when(robotActor).advanceState(any(), any());

    listener.onRobotStateAdvancementRequested(new AdvanceRobotStateRequest(robotActor, randomAdvance));

    verify(robotActor).advanceState(same(robotMap), same(randomAdvance));
    verify(applicationEventPublisher, never()).publishEvent(any());
  }
}
