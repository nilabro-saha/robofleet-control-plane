package com.robofleet.robotsimulator.robot.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.robofleet.robotsimulator.robot.domain.RobotActor;
import com.robofleet.robotsimulator.robot.domain.RobotStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class RobotRegistryTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @Test
  void register_shouldIncludeRobotInSnapshot() {
    RobotRegistry robotRegistry = new RobotRegistry(applicationEventPublisher);
    RobotActor robotActor = RobotActor.builder()
        .robotId("robot-1")
        .positionX(1.0)
        .positionY(2.0)
        .battery(50.0)
        .status(RobotStatus.IDLE)
        .build();

    robotRegistry.register(robotActor);

    List<RobotActor> registeredRobots = robotRegistry.getRegisteredRobots();
    assertEquals(1, registeredRobots.size());
    assertEquals("robot-1", registeredRobots.getFirst().getRobotId());
    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotCreatedEvent.class));
  }

  @Test
  void getRegisteredRobots_shouldReturnImmutableSnapshot() {
    RobotRegistry robotRegistry = new RobotRegistry(applicationEventPublisher);
    RobotActor robotActor = RobotActor.builder()
        .robotId("robot-2")
        .positionX(1.0)
        .positionY(2.0)
        .battery(50.0)
        .status(RobotStatus.IDLE)
        .build();
    robotRegistry.register(robotActor);

    List<RobotActor> registeredRobots = robotRegistry.getRegisteredRobots();

    assertThrows(UnsupportedOperationException.class, () -> registeredRobots.add(robotActor));
  }

  @Test
  void clearAndGetRemovedRobots_shouldEmptyRegistryAndReturnRemovedSnapshot() {
    RobotRegistry robotRegistry = new RobotRegistry(applicationEventPublisher);
    RobotActor robotActor = RobotActor.builder()
        .robotId("robot-3")
        .positionX(1.0)
        .positionY(2.0)
        .battery(50.0)
        .status(RobotStatus.IDLE)
        .build();
    robotRegistry.register(robotActor);

    List<RobotActor> removed = robotRegistry.clearAndGetRemovedRobots();

    assertEquals(1, removed.size());
    assertEquals("robot-3", removed.getFirst().getRobotId());
    assertEquals(0, robotRegistry.getRegisteredRobots().size());
    assertThrows(UnsupportedOperationException.class, () -> removed.add(robotActor));
    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotCreatedEvent.class));
    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotDeletedEvent.class));
  }

  @Test
  void deregisterById_shouldRemoveRobotAndPublishLifecycle() {
    RobotRegistry robotRegistry = new RobotRegistry(applicationEventPublisher);
    RobotActor robotActor = RobotActor.builder()
        .robotId("robot-4")
        .positionX(1.0)
        .positionY(2.0)
        .battery(50.0)
        .status(RobotStatus.IDLE)
        .build();
    robotRegistry.register(robotActor);

    boolean removed = robotRegistry.deregisterById("robot-4");

    assertEquals(true, removed);
    assertEquals(0, robotRegistry.getRegisteredRobots().size());
    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotCreatedEvent.class));
    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotDeletedEvent.class));
  }
}
