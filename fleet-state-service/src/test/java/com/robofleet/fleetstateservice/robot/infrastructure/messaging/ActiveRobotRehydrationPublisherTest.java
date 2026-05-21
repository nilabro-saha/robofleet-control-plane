package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.robofleet.fleetstateservice.robot.application.RobotCreationCommandGateway;
import com.robofleet.fleetstateservice.robot.domain.Robot;
import com.robofleet.fleetstateservice.robot.domain.RobotLifecycleStatus;
import com.robofleet.fleetstateservice.robot.domain.RobotState;
import com.robofleet.fleetstateservice.robot.infrastructure.persistence.RobotRepository;
import com.robofleet.fleetstateservice.robot.infrastructure.persistence.RobotStateRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActiveRobotRehydrationPublisherTest {

  @Mock
  private RobotRepository robotRepository;

  @Mock
  private RobotCreationCommandGateway robotCreationCommandGateway;

  @Mock
  private RobotStateRepository robotStateRepository;

  @InjectMocks
  private ActiveRobotRehydrationPublisher publisher;

  @Test
  void publishRehydrationCommands_shouldPublishOneEventPerActiveRobot() {
    Instant firstTimestamp = Instant.parse("2026-05-21T10:00:00Z");
    Instant secondTimestamp = Instant.parse("2026-05-21T10:00:01Z");

    when(robotRepository.findByLifecycleStatus(RobotLifecycleStatus.ACTIVE)).thenReturn(List.of(
        Robot.builder()
            .robotId("robot-1")
            .displayName("A")
            .lifecycleStatus(RobotLifecycleStatus.ACTIVE)
            .build(),
        Robot.builder()
            .robotId("robot-2")
            .displayName("B")
            .lifecycleStatus(RobotLifecycleStatus.ACTIVE)
            .build()
    ));
    when(robotStateRepository.findById("robot-1")).thenReturn(Optional.of(
        RobotState.builder()
            .robotId("robot-1")
            .positionX(12.34)
            .positionY(56.78)
            .battery(87.1)
            .status("MOVING")
            .timestamp(firstTimestamp)
            .build()
    ));
    when(robotStateRepository.findById("robot-2")).thenReturn(Optional.of(
        RobotState.builder()
            .robotId("robot-2")
            .positionX(21.0)
            .positionY(43.0)
            .battery(65.5)
            .status("IDLE")
            .timestamp(secondTimestamp)
            .build()
    ));

    publisher.publishRehydrationCommands();

    ArgumentCaptor<RobotLifecycleEvent> captor = ArgumentCaptor.forClass(RobotLifecycleEvent.class);
    verify(robotCreationCommandGateway, times(2))
        .publishLifecycleEvent(captor.capture());

    List<RobotLifecycleEvent> events = captor.getAllValues();
    assertEquals("robot-1", events.get(0).getRobotId());
    assertEquals("robot-2", events.get(1).getRobotId());
    assertEquals(
        LifecycleEventType.REHYDRATE_ACTIVE,
        events.get(0).getEventType());
    assertEquals(
        "robot-lifecycle-changed",
        events.get(0).getEventName());
    assertEquals(
        LifecycleEventType.REHYDRATE_ACTIVE,
        events.get(1).getEventType());
    assertEquals(
        "robot-lifecycle-changed",
        events.get(1).getEventName());
    assertEquals(12.34, events.get(0).getPositionX());
    assertEquals(56.78, events.get(0).getPositionY());
    assertEquals(87.1, events.get(0).getBattery());
    assertEquals("MOVING", events.get(0).getStatus());
    assertEquals(firstTimestamp, events.get(0).getTimestamp());
    assertNotNull(events.get(0).getCorrelationId());
    assertNotNull(events.get(1).getCorrelationId());
  }

  @Test
  void publishRehydrationCommands_shouldDoNothingWhenNoActiveRobots() {
    when(robotRepository.findByLifecycleStatus(RobotLifecycleStatus.ACTIVE)).thenReturn(List.of());

    publisher.publishRehydrationCommands();

    verify(robotCreationCommandGateway, never()).publishLifecycleEvent(any());
  }

  @Test
  void publishRehydrationCommands_shouldPublishWithoutStateWhenSnapshotMissing() {
    when(robotRepository.findByLifecycleStatus(RobotLifecycleStatus.ACTIVE)).thenReturn(List.of(
        Robot.builder()
            .robotId("robot-1")
            .displayName("A")
            .lifecycleStatus(RobotLifecycleStatus.ACTIVE)
            .build()
    ));
    when(robotStateRepository.findById("robot-1")).thenReturn(Optional.empty());

    publisher.publishRehydrationCommands();

    ArgumentCaptor<RobotLifecycleEvent> captor = ArgumentCaptor.forClass(RobotLifecycleEvent.class);
    verify(robotCreationCommandGateway).publishLifecycleEvent(captor.capture());
    RobotLifecycleEvent event = captor.getValue();
    assertEquals("robot-1", event.getRobotId());
    assertNull(event.getPositionX());
    assertNull(event.getPositionY());
    assertNull(event.getBattery());
    assertNull(event.getStatus());
    assertEquals("robot-lifecycle-changed", event.getEventName());
    assertNotNull(event.getCorrelationId());
  }
}
