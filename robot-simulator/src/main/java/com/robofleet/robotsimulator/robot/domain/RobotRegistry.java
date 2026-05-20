package com.robofleet.robotsimulator.robot.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;

/**
 * Central registry representing a bounded rectangular map and known robots.
 */
@Getter
public class RobotRegistry {

  private final List<RobotActor> robots = new ArrayList<>();
  private final Lock robotsLock = new ReentrantLock();

  /**
   * Registers one robot actor with the central registry.
   */
  public void register(RobotActor robotActor) {
    robotsLock.lock();
    try {
      robots.add(robotActor);
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
}
