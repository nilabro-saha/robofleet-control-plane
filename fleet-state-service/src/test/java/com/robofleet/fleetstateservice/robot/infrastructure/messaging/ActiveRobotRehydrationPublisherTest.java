package com.robofleet.fleetstateservice.robot.infrastructure.messaging;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.robofleet.fleetstateservice.robot.application.RobotCreationCommandGateway;
import com.robofleet.fleetstateservice.robot.domain.Robot;
import com.robofleet.fleetstateservice.robot.domain.RobotLifecycleStatus;
import com.robofleet.fleetstateservice.robot.infrastructure.persistence.RobotRepository;
import java.util.List;
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

  @InjectMocks
  private ActiveRobotRehydrationPublisher publisher;

  @Test
  void publishRehydrationCommands_shouldPublishOneEventPerActiveRobot() {
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

    publisher.publishRehydrationCommands();

    ArgumentCaptor<RobotLifecycleEvent> captor = ArgumentCaptor.forClass(RobotLifecycleEvent.class);
    verify(robotCreationCommandGateway, org.mockito.Mockito.times(2))
        .publishLifecycleEvent(captor.capture());

    List<RobotLifecycleEvent> events = captor.getAllValues();
    org.junit.jupiter.api.Assertions.assertEquals("robot-1", events.get(0).getRobotId());
    org.junit.jupiter.api.Assertions.assertEquals("robot-2", events.get(1).getRobotId());
    org.junit.jupiter.api.Assertions.assertEquals(
        LifecycleEventType.REHYDRATE_ACTIVE,
        events.get(0).getEventType());
    org.junit.jupiter.api.Assertions.assertEquals(
        LifecycleEventType.REHYDRATE_ACTIVE,
        events.get(1).getEventType());
  }

  @Test
  void publishRehydrationCommands_shouldDoNothingWhenNoActiveRobots() {
    when(robotRepository.findByLifecycleStatus(RobotLifecycleStatus.ACTIVE)).thenReturn(List.of());

    publisher.publishRehydrationCommands();

    verify(robotCreationCommandGateway, never()).publishLifecycleEvent(org.mockito.ArgumentMatchers.any());
  }
}
