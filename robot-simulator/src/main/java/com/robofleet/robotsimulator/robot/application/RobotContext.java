package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.robot.domain.RobotActor;
import com.robofleet.robotsimulator.robot.domain.RobotRegistry;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Shared robot application context used by robot actors to request instrumentation.
 */
@Component
@RequiredArgsConstructor
public class RobotContext {

  private final ApplicationEventPublisher applicationEventPublisher;
  private final RobotRegistry robotRegistry;
  private final RobotMap robotMap;

  /**
   * Registers the provided robot actor in the central registry.
   */
  public void register(RobotActor robotActor) {
    robotRegistry.register(robotActor);
  }

  /**
   * Requests telemetry instrumentation for the provided robot actor.
   */
  public void instrumentalize(RobotActor robotActor) {
    applicationEventPublisher.publishEvent(new RobotTelemetryInstrumentationRequestedEvent(robotActor));
  }

  /**
   * Requests random state advancement scheduling for the provided robot actor.
   */
  public void requestStateAdvancement(RobotActor robotActor) {
    applicationEventPublisher.publishEvent(new RobotStateAdvancementRequestedEvent(robotActor));
  }

  /**
   * Returns current robot map.
   */
  public RobotMap robotMap() {
    return robotMap;
  }
}
