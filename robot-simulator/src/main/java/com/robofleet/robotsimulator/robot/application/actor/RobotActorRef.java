package com.robofleet.robotsimulator.robot.application.actor;

import com.robofleet.robotsimulator.robot.application.event.RobotAdvancedEvent;
import com.robofleet.robotsimulator.robot.domain.map.MapLocation;
import com.robofleet.robotsimulator.robot.domain.map.RectangularMap;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import com.robofleet.robotsimulator.robot.domain.model.RobotActor;
import com.robofleet.robotsimulator.robot.domain.model.RobotState;
import com.robofleet.robotsimulator.robot.domain.model.RobotStatus;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Actor-reference wrapper for one {@link RobotActor} instance.
 *
 * <p>Intent: centralize robot command handling so external schedulers and adapters interact via
 * message passing ({@code tell}) instead of mutating robot state directly.</p>
 *
 * @author Nilabro Saha
 */
@Slf4j
@RequiredArgsConstructor
public class RobotActorRef implements ActorRef<RobotCommand> {

  private final RobotActor robotActor;
  private final RobotMap robotMap;
  private final ApplicationEventPublisher applicationEventPublisher;

  /**
   * Creates one robot actor reference from an explicitly provided fixed state.
   *
   * @param fixedState requested robot state with explicit values
   * @param robotMap map used for clamping resolution
   * @param applicationEventPublisher publisher for lifecycle/telemetry events
   * @return actor reference with normalized robot actor state
   */
  public static RobotActorRef fromFixedState(
      RobotState fixedState,
      RobotMap robotMap,
      ApplicationEventPublisher applicationEventPublisher
  ) {
    double resolvedPositionX = clampX(robotMap, fixedState.positionX());
    double resolvedPositionY = clampY(robotMap, fixedState.positionY());
    double resolvedBattery = clampBattery(fixedState.battery());

    RobotActor robotActor = RobotActor.builder()
        .robotId(fixedState.robotId())
        .positionX(resolvedPositionX)
        .positionY(resolvedPositionY)
        .battery(resolvedBattery)
        .status(fixedState.status())
        .build();

    return new RobotActorRef(robotActor, robotMap, applicationEventPublisher);
  }

  /**
   * Returns a read-only state view for the managed actor.
   */
  public RobotState lastState() {
    return robotActor.lastState();
  }

  /**
   * Creates one robot actor reference with randomized spawn state.
   *
   * @param robotId robot identifier
   * @param robotMap map used for random spawn resolution
   * @param applicationEventPublisher publisher for lifecycle/telemetry events
   * @return actor reference with randomized robot actor state
   */
  public static RobotActorRef fromRandomState(
      String robotId,
      ThreadLocalRandom random,
      RobotMap robotMap,
      ApplicationEventPublisher applicationEventPublisher
  ) {
    MapLocation spawnLocation = robotMap.randomAvailableLocation(random);

    RobotActor robotActor = RobotActor.builder()
        .robotId(robotId)
        .positionX(spawnLocation.x())
        .positionY(spawnLocation.y())
        .battery(random.nextDouble(20.0, 100.0))
        .status(RobotStatus.values()[random.nextInt(RobotStatus.values().length)])
        .build();

    return new RobotActorRef(robotActor, robotMap, applicationEventPublisher);
  }

  @Override
  public void tell(RobotCommand message) {
    switch (message) {
      case RobotCommand.AdvanceState arsc -> handleAdvanceState(arsc);
    }
  }

  private void handleAdvanceState(RobotCommand.AdvanceState command) {
    try {
      robotActor.advanceState(robotMap, command.advancementMode());
      applicationEventPublisher.publishEvent(new RobotAdvancedEvent(lastState()));
    } catch (Exception e) {
      log.error("Failed to process advance-state command for {}", robotActor.getRobotId(), e);
    }
  }

  private static double clampX(RobotMap map, double x) {
    return switch (map) {
      case RectangularMap rm -> rm.clampX(x);
      case null -> x;
    };
  }

  private static double clampY(RobotMap map, double y) {
    return switch (map) {
      case RectangularMap rm -> rm.clampY(y);
      case null -> y;
    };
  }

  private static double clampBattery(double value) {
    return Math.clamp(value, 0.0, 100.0);
  }
}
