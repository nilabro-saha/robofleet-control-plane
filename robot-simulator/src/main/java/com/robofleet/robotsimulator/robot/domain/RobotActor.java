package com.robofleet.robotsimulator.robot.domain;

import com.robofleet.robotsimulator.robot.application.RobotContext;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.RobotTelemetryEvent;
import com.robofleet.robotsimulator.robot.domain.map.RectangularMap;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Mutable actor representing one robot with internal state.
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

  private double x;
  private double y;
  private double battery;
  private RobotStatus status;
  @Builder.Default
  private final Lock stateLock = new ReentrantLock();

  /**
   * Requests simulator instrumentation (telemetry scheduling) for this robot.
   */
  public void instrument(RobotContext robotContext) {
    robotContext.instrumentalize(this);
  }

  /**
   * Requests simulator scheduling for random state advancement for this robot.
   */
  public void scheduleRandomAdvancement(RobotContext robotContext) {
    robotContext.requestStateAdvancement(this);
  }

  /**
   * Registers this robot actor with the central registry via context.
   */
  public void registerWith(RobotContext robotContext) {
    robotContext.register(this);
  }

  /**
   * Produces telemetry for the current in-memory state without advancing it.
   */
  public RobotTelemetryEvent currentTelemetry() {
    stateLock.lock();
    try {
      return RobotTelemetryEvent.builder()
          .robotId(robotId)
          .positionX(roundTwo(x))
          .positionY(roundTwo(y))
          .battery(roundTwo(battery))
          .status(status.name())
          .timestamp(Instant.now())
          .build();
    } finally {
      stateLock.unlock();
    }
  }

  /**
   * Advances robot state for the next publish cycle.
   */
  public void advanceState(RobotContext robotContext) {
    stateLock.lock();
    try {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      RobotMap map = robotContext.robotMap();
      if (!(map instanceof RectangularMap rectangularMap)) {
        throw new IllegalStateException("Unsupported map type for robot movement: " + map);
      }

      if (status == RobotStatus.MOVING) {
        x = rectangularMap.clampX(x + random.nextDouble(-3.0, 3.0));
        y = rectangularMap.clampY(y + random.nextDouble(-3.0, 3.0));
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
