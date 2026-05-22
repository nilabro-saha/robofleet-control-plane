package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.config.SimulatorProperties;
import com.robofleet.robotsimulator.robot.application.command.AdvanceRobotStateRequest;
import com.robofleet.robotsimulator.robot.domain.behavior.RandomAdvance;
import com.robofleet.robotsimulator.robot.domain.model.RobotActor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Schedules periodic requests to advance robot state.
 *
 * <p>Intent: decouple time-based advancement triggering from advancement execution so scheduling
 * policy can evolve independently from state-transition logic.</p>
 *
 * @author Nilabro Saha
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdvanceRobotStateScheduler {

  private final ScheduledExecutorService robotTelemetryScheduler;
  private final SimulatorProperties simulatorProperties;
  private final RobotRegistry robotRegistry;
  private final ApplicationEventPublisher applicationEventPublisher;

  /**
   * Starts a periodic scheduler that emits one state-advance request per robot.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void startScheduling() {
    robotTelemetryScheduler.scheduleAtFixedRate(
        this::publishAdvanceRequests,
        0,
        simulatorProperties.getStateAdvanceIntervalMs(),
        TimeUnit.MILLISECONDS
    );

    log.info(
        "Registered advance-state scheduler (advance={}ms)",
        simulatorProperties.getStateAdvanceIntervalMs()
    );
  }

  private void publishAdvanceRequests() {
    for (RobotActor robotActor : robotRegistry.getRegisteredRobots()) {
      applicationEventPublisher.publishEvent(
          new AdvanceRobotStateRequest(robotActor, new RandomAdvance(ThreadLocalRandom.current())));
    }
  }
}
