package com.robofleet.robotsimulator.robot.domain.model;

import com.robofleet.robotsimulator.robot.domain.behavior.AdvancementMode;
import com.robofleet.robotsimulator.robot.domain.behavior.HoldPositionAdvance;
import com.robofleet.robotsimulator.robot.domain.behavior.PathFollowingAdvance;
import com.robofleet.robotsimulator.robot.domain.behavior.RandomAdvance;
import com.robofleet.robotsimulator.robot.domain.map.RectangularMap;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.random.RandomGenerator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Mutable actor representing one robot with internal state.
 *
 * @author Nilabro Saha
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RobotActor {

  private final String robotId;
  private double positionX;
  private double positionY;
  private double battery;
  private RobotStatus status;
  @Builder.Default
  private final Lock stateLock = new ReentrantLock();

  /**
   * Produces a telemetry view of current in-memory state without advancing it.
   */
  public RobotState lastState() {
    stateLock.lock();
    try {
      return new RobotState(robotId, positionX, positionY, battery, status);
    } finally {
      stateLock.unlock();
    }
  }

  /**
   * Advances robot state for the next publish cycle.
   */
  public void advanceState(RobotMap map, AdvancementMode advancementMode) {
    stateLock.lock();
    try {
      switch (advancementMode) {
        case RandomAdvance(var random) -> {
          switch (map) {
            case RectangularMap rm -> advanceRandomlyInRectangularMap(random, rm);
            case null -> throw new IllegalStateException("Invalid map: " + map);
          }
        }
        case HoldPositionAdvance hpa ->
            throw new IllegalStateException("HoldPositionAdvance not yet supported");
        case PathFollowingAdvance pfa ->
            throw new IllegalStateException("PathFollowingAdvance not yet supported");
      }
    } finally {
      stateLock.unlock();
    }
  }

  private void advanceRandomlyInRectangularMap(
      RandomGenerator random,
      RectangularMap rectangularMap
  ) {
    switch (status) {
      case RobotStatus.MOVING -> {
        positionX = rectangularMap.clampX(positionX + random.nextDouble(-3.0, 3.0));
        positionY = rectangularMap.clampY(positionY + random.nextDouble(-3.0, 3.0));
        battery = Math.clamp(battery - random.nextDouble(0.3, 1.5), 0.0, 100.0);
      }
      case RobotStatus.CHARGING ->
          battery = Math.clamp(battery + random.nextDouble(1.0, 3.0), 0.0, 100.0);
      default ->
          battery = Math.clamp(battery - random.nextDouble(0.05, 0.2), 0.0, 100.0);
    }

    if (battery < 15.0) {
      status = RobotStatus.CHARGING;
    } else if (battery > 90.0 && status == RobotStatus.CHARGING) {
      status = RobotStatus.IDLE;
    } else if (random.nextDouble() < 0.07) {
      status = RobotStatus.values()[random.nextInt(RobotStatus.values().length)];
    }
  }

}
