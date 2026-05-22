package com.robofleet.robotsimulator.robot.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.robofleet.robotsimulator.robot.application.actor.RobotCommand;
import com.robofleet.robotsimulator.robot.application.actor.RobotOrchestration;
import com.robofleet.robotsimulator.robot.application.actor.RobotState;
import com.robofleet.robotsimulator.robot.application.event.RobotAdvancedEvent;
import com.robofleet.robotsimulator.robot.application.event.RobotCreatedEvent;
import com.robofleet.robotsimulator.robot.application.event.RobotDeletedEvent;
import com.robofleet.robotsimulator.robot.domain.behavior.RandomAdvance;
import com.robofleet.robotsimulator.robot.domain.map.RectangularMap;
import com.robofleet.robotsimulator.robot.domain.model.RobotStatus;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class RobotOrchestratorTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  private final RectangularMap robotMap = new RectangularMap(0.0, 100.0, 0.0, 100.0);

  @Test
  void register_shouldIncreaseRegistryCountAndPublishCreateEvent() {
    RobotOrchestrator robotOrchestrator = new RobotOrchestrator(robotMap, applicationEventPublisher);
    robotOrchestrator.tell(new RobotOrchestration.Spawn(
        new RobotState("robot-1", 1.0, 2.0, 50.0, RobotStatus.IDLE)
    ));

    robotOrchestrator.tell(new RobotOrchestration.DestroyAll());

    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotCreatedEvent.class));
    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotDeletedEvent.class));
  }

  @Test
  void registerDuplicate_shouldNoOp() {
    RobotOrchestrator robotOrchestrator = new RobotOrchestrator(robotMap, applicationEventPublisher);
    robotOrchestrator.tell(new RobotOrchestration.Spawn(
        new RobotState("robot-2", 1.0, 2.0, 50.0, RobotStatus.IDLE)
    ));
    robotOrchestrator.tell(new RobotOrchestration.Spawn(
        new RobotState("robot-2", 3.0, 4.0, 40.0, RobotStatus.MOVING)
    ));

    robotOrchestrator.tell(new RobotOrchestration.DestroyAll());

    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotCreatedEvent.class));
    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotDeletedEvent.class));
  }

  @Test
  void clearRegistry_shouldEmptyRegistryAndReturnRemovedCount() {
    RobotOrchestrator robotOrchestrator = new RobotOrchestrator(robotMap, applicationEventPublisher);
    robotOrchestrator.tell(new RobotOrchestration.Spawn(
        new RobotState("robot-3", 1.0, 2.0, 50.0, RobotStatus.IDLE)
    ));

    robotOrchestrator.tell(new RobotOrchestration.DestroyAll());

    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotCreatedEvent.class));
    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotDeletedEvent.class));
  }

  @Test
  void deregisterById_shouldRemoveRobotAndPublishLifecycle() {
    RobotOrchestrator robotOrchestrator = new RobotOrchestrator(robotMap, applicationEventPublisher);
    robotOrchestrator.tell(new RobotOrchestration.Spawn(
        new RobotState("robot-4", 1.0, 2.0, 50.0, RobotStatus.IDLE)
    ));

    robotOrchestrator.tell(new RobotOrchestration.Destroy("robot-4"));
    robotOrchestrator.tell(new RobotOrchestration.DestroyAll());

    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotCreatedEvent.class));
    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotDeletedEvent.class));
  }

  @Test
  void spawnRandom_shouldCreateAndDeleteRobotLifecycleEvents() {
    RobotOrchestrator robotOrchestrator = new RobotOrchestrator(robotMap, applicationEventPublisher);

    robotOrchestrator.tell(new RobotOrchestration.SpawnRandom("robot-random-1"));
    robotOrchestrator.tell(new RobotOrchestration.DestroyAll());

    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotCreatedEvent.class));
    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotDeletedEvent.class));
  }

  @Test
  void destroyUnknown_shouldNotDeleteAnythingUntilDestroyAll() {
    RobotOrchestrator robotOrchestrator = new RobotOrchestrator(robotMap, applicationEventPublisher);
    robotOrchestrator.tell(new RobotOrchestration.Spawn(
        new RobotState("robot-unknown-check", 1.0, 2.0, 50.0, RobotStatus.IDLE)
    ));

    robotOrchestrator.tell(new RobotOrchestration.Destroy("missing-robot"));

    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotCreatedEvent.class));
    verify(applicationEventPublisher, never()).publishEvent(any(RobotDeletedEvent.class));

    robotOrchestrator.tell(new RobotOrchestration.DestroyAll());

    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotDeletedEvent.class));
  }

  @Test
  void tellAll_shouldPublishAdvancedEventForRegisteredRobot() {
    RobotOrchestrator robotOrchestrator = new RobotOrchestrator(robotMap, applicationEventPublisher);
    robotOrchestrator.tell(new RobotOrchestration.Spawn(
        new RobotState("robot-advance", 10.0, 10.0, 75.0, RobotStatus.MOVING)
    ));

    robotOrchestrator.tell(
        new RobotOrchestration.TellAll(
            new RobotCommand.AdvanceState(
                new RandomAdvance(ThreadLocalRandom.current())
            )
        )
    );

    verify(applicationEventPublisher, times(1)).publishEvent(any(RobotAdvancedEvent.class));
  }
}