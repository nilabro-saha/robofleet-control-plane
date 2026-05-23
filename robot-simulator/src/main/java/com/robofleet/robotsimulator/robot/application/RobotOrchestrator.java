package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.robot.application.actor.ActorRef;
import com.robofleet.robotsimulator.robot.application.actor.RobotActorRef;
import com.robofleet.robotsimulator.robot.application.actor.RobotCommand;
import com.robofleet.robotsimulator.robot.application.actor.RobotOrchestration;
import com.robofleet.robotsimulator.robot.application.event.RobotCreatedEvent;
import com.robofleet.robotsimulator.robot.application.event.RobotDeletedEvent;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import com.robofleet.robotsimulator.robot.domain.model.RobotState;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Central orchestrator representing active simulator robots.
 *
 * <p>Intent: provide a thread-safe source of truth for currently active robot actors and publish
 * lifecycle transition events whenever membership changes.</p>
 *
 * @author Nilabro Saha
 */
@Slf4j
public class RobotOrchestrator implements ActorRef<RobotOrchestration> {

  private final List<RobotActorRef> robotActorRefs = new ArrayList<>();
  private final Lock robotsLock = new ReentrantLock();
  private final RobotMap robotMap;
  private final ApplicationEventPublisher applicationEventPublisher;

  /**
   * Creates an orchestrator bound to the provided application event publisher.
   *
   * @param robotMap map used by actor references to process movement commands
   * @param applicationEventPublisher publisher used for created/deleted robot events
   */
  public RobotOrchestrator(
      RobotMap robotMap,
      ApplicationEventPublisher applicationEventPublisher
  ) {
    this.robotMap = robotMap;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public void tell(RobotOrchestration message) {
    switch (message) {
      case RobotOrchestration.Spawn(var state, var corrId) -> spawnInternal(state, corrId);
      case RobotOrchestration.SpawnRandom(var rid, var cid) -> spawnRandomInternal(rid, cid);
      case RobotOrchestration.Destroy(var rid, var cid) -> destroyInternal(rid, cid);
      case RobotOrchestration.DestroyAll ignored -> destroyAllInternal();
      case RobotOrchestration.TellAll(var robotCommand) -> tellAllRobotsInternal(robotCommand);
    }
  }

  private void spawnInternal(RobotState requestedState, String correlationId) {
    robotsLock.lock();
    try {
      boolean alreadyRegistered = robotActorRefs.stream()
          .anyMatch(ref -> ref.lastState().robotId().equals(requestedState.robotId()));
      if (alreadyRegistered) {
        log.info("Robot {} already registered, skipping create command", requestedState.robotId());
        return;
      }

      RobotActorRef robotActorRef = RobotActorRef.fromFixedState(
          requestedState,
          robotMap,
          applicationEventPublisher
      );
      robotActorRefs.add(robotActorRef);
      var robotView = robotActorRef.lastState();
      applicationEventPublisher.publishEvent(new RobotCreatedEvent(robotView, correlationId));
      log.info(
          "Registered command-driven robot {} at ({}, {})",
          robotView.robotId(),
          round(robotView.positionX()),
          round(robotView.positionY())
      );
    } finally {
      robotsLock.unlock();
    }
  }

  private void spawnRandomInternal(String robotId, String correlationId) {
    robotsLock.lock();
    try {
      boolean alreadyRegistered = robotActorRefs.stream()
          .anyMatch(ref -> ref.lastState().robotId().equals(robotId));
      if (alreadyRegistered) {
        log.info("Robot {} already registered, skipping create command", robotId);
        return;
      }

      RobotActorRef robotActorRef = RobotActorRef.fromRandomState(
          robotId,
          ThreadLocalRandom.current(),
          robotMap,
          applicationEventPublisher
      );
      robotActorRefs.add(robotActorRef);
      var robotView = robotActorRef.lastState();
      applicationEventPublisher.publishEvent(new RobotCreatedEvent(robotView, correlationId));
      log.info(
          "Registered random-state robot {} at ({}, {})",
          robotView.robotId(),
          round(robotView.positionX()),
          round(robotView.positionY())
      );
    } finally {
      robotsLock.unlock();
    }
  }

  private void destroyAllInternal() {
    robotsLock.lock();
    try {
      List<RobotDeletedEvent> deletedEvents = robotActorRefs.stream()
          .map(robotActorRef -> new RobotDeletedEvent(robotActorRef.lastState(), null))
          .toList();
      robotActorRefs.clear();
      for (var deletedEvent : deletedEvents) {
        applicationEventPublisher.publishEvent(deletedEvent);
      }
      log.info("Cleared orchestrator and removed {} robots", deletedEvents.size());
    } finally {
      robotsLock.unlock();
    }
  }

  private void destroyInternal(String robotId, String correlationId) {
    robotsLock.lock();
    try {
      RobotActorRef removedRobotRef = null;
      for (RobotActorRef robotActorRef : robotActorRefs) {
        if (robotActorRef.lastState().robotId().equals(robotId)) {
          removedRobotRef = robotActorRef;
          break;
        }
      }

      if (removedRobotRef != null) {
        robotActorRefs.remove(removedRobotRef);
        applicationEventPublisher.publishEvent(
            new RobotDeletedEvent(removedRobotRef.lastState(), correlationId)
        );
        log.info("Deregistered command-driven robot {}", robotId);
        return;
      }
      log.info("Robot {} not registered, skipping delete command", robotId);
    } finally {
      robotsLock.unlock();
    }
  }

  private void tellAllRobotsInternal(RobotCommand robotCommand) {
    List<RobotActorRef> robotActorRefsSnapshot;
    robotsLock.lock();
    try {
      robotActorRefsSnapshot = List.copyOf(robotActorRefs);
    } finally {
      robotsLock.unlock();
    }

    for (RobotActorRef robotActorRef : robotActorRefsSnapshot) {
      robotActorRef.tell(robotCommand);
    }
  }

  private double round(double value) {
    return Math.round(value * 100.0) / 100.0;
  }

}