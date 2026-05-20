package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.robot.domain.RobotActor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Central registry representing active simulator robots.
 */
@Getter
public class RobotRegistry {

  private final List<RobotActor> robots = new ArrayList<>();
  private final Lock robotsLock = new ReentrantLock();
  private final ApplicationEventPublisher applicationEventPublisher;

  public RobotRegistry(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  /**
   * Registers one robot actor and emits lifecycle CREATED.
   */
  public void register(RobotActor robotActor) {
    robotsLock.lock();
    try {
      robots.add(robotActor);
      applicationEventPublisher.publishEvent(new RobotCreatedEvent(robotActor));
    } finally {
      robotsLock.unlock();
    }
  }

  /**
   * Returns immutable snapshot of registered robots.
   */
  public List<RobotActor> getRegisteredRobots() {
    robotsLock.lock();
    try {
      return Collections.unmodifiableList(new ArrayList<>(robots));
    } finally {
      robotsLock.unlock();
    }
  }

  /**
   * Removes all registered robots and emits lifecycle REMOVED events.
   */
  public List<RobotActor> clearAndGetRemovedRobots() {
    robotsLock.lock();
    try {
      List<RobotActor> removedRobots = new ArrayList<>(robots);
      robots.clear();
      for (RobotActor removedRobot : removedRobots) {
        applicationEventPublisher.publishEvent(new RobotDeletedEvent(removedRobot));
      }
      return Collections.unmodifiableList(removedRobots);
    } finally {
      robotsLock.unlock();
    }
  }

  /**
   * Removes one robot by id and emits lifecycle REMOVED when present.
   */
  public boolean deregisterById(String robotId) {
    robotsLock.lock();
    try {
      RobotActor removedRobot = null;
      for (RobotActor robot : robots) {
        if (robot.getRobotId().equals(robotId)) {
          removedRobot = robot;
          break;
        }
      }

      if (removedRobot != null) {
        robots.remove(removedRobot);
        applicationEventPublisher.publishEvent(new RobotDeletedEvent(removedRobot));
        return true;
      }
      return false;
    } finally {
      robotsLock.unlock();
    }
  }

}
