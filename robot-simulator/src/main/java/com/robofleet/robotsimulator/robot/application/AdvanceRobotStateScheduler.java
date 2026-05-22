package com.robofleet.robotsimulator.robot.application;

import com.robofleet.robotsimulator.config.SimulatorProperties;
import com.robofleet.robotsimulator.robot.application.actor.RobotCommand;
import com.robofleet.robotsimulator.robot.application.actor.RobotOrchestration;
import com.robofleet.robotsimulator.robot.domain.behavior.RandomAdvance;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
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
  private final RobotOrchestrator robotOrchestrator;

  /**
   * Starts a periodic scheduler that emits one state-advance request per robot.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void startScheduling() {
    robotTelemetryScheduler.scheduleAtFixedRate(
        () -> robotOrchestrator.tell(
            new RobotOrchestration.TellAll(
                new RobotCommand.AdvanceState(new RandomAdvance(ThreadLocalRandom.current()))
            )
        ),
        0,
        simulatorProperties.getStateAdvanceIntervalMs(),
        TimeUnit.MILLISECONDS
    );

    log.info(
        "Registered advance-state scheduler (advance={}ms)",
        simulatorProperties.getStateAdvanceIntervalMs()
    );
  }

}
