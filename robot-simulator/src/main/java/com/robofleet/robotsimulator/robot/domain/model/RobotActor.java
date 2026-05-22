package com.robofleet.robotsimulator.robot.domain.model;

import com.robofleet.robotsimulator.robot.domain.behavior.AdvancementMode;
import com.robofleet.robotsimulator.robot.domain.behavior.RandomAdvance;
import com.robofleet.robotsimulator.robot.domain.map.RectangularMap;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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

  private static final List<RobotStatus> RANDOM_STATUSES = List.of(
      RobotStatus.IDLE,
      RobotStatus.MOVING,
      RobotStatus.CHARGING,
      RobotStatus.ERROR
  );

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
  public LastStateView lastStateView() {
    stateLock.lock();
    try {
      return new LastStateView(
          robotId,
          roundTwo(positionX),
          roundTwo(positionY),
          roundTwo(battery),
          status.name(),
          Instant.now()
      );
    } finally {
      stateLock.unlock();
    }
  }

  /**
   * Immutable view of robot state used by application listeners.
   */
  public record LastStateView(
      String robotId,
      double positionX,
      double positionY,
      double battery,
      String status,
      Instant timestamp
  ) {
  }

  /**
   * Advances robot state for the next publish cycle.
   */
  public void advanceState(RobotMap map, AdvancementMode advancementMode) {
    stateLock.lock();
    try {
      if (!(advancementMode instanceof RandomAdvance(var random))) {
        throw new IllegalStateException("Unsupported advancement mode: " + advancementMode);
      }
      if (!(map instanceof RectangularMap rectangularMap)) {
        throw new IllegalStateException("Unsupported map type for robot movement: " + map);
      }

      if (status == RobotStatus.MOVING) {
        positionX = rectangularMap.clampX(positionX + random.nextDouble(-3.0, 3.0));
        positionY = rectangularMap.clampY(positionY + random.nextDouble(-3.0, 3.0));
        battery = clamp(battery - random.nextDouble(0.3, 1.5), 0.0, 100.0);
      } else if (status == RobotStatus.CHARGING) {
        battery = clamp(battery + random.nextDouble(1.0, 3.0), 0.0, 100.0);
      } else {
        battery = clamp(battery - random.nextDouble(0.05, 0.2), 0.0, 100.0);
      }

      if (battery < 15.0) {
        status = RobotStatus.CHARGING;
      } else if (battery > 90.0 && status == RobotStatus.CHARGING) {
        status = RobotStatus.IDLE;
      } else if (random.nextDouble() < 0.07) {
        status = RANDOM_STATUSES.get(random.nextInt(RANDOM_STATUSES.size()));
      }
    } finally {
      stateLock.unlock();
    }
  }

  private double clamp(double value, double min, double max) {
    return Math.clamp(value, min, max);
  }

  private double roundTwo(double value) {
    return Math.round(value * 100.0) / 100.0;
  }
}
