package com.robofleet.robotsimulator.robot.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.robofleet.robotsimulator.config.SimulatorProperties;
import com.robofleet.robotsimulator.robot.application.command.AdvanceRobotStateRequest;
import com.robofleet.robotsimulator.robot.domain.behavior.AdvancementMode;
import com.robofleet.robotsimulator.robot.domain.model.RobotActor;
import com.robofleet.robotsimulator.robot.domain.model.RobotStatus;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AdvanceRobotStateSchedulerTest {

  @Mock
  private ScheduledExecutorService robotTelemetryScheduler;
  @Mock
  private SimulatorProperties simulatorProperties;
  @Mock
  private RobotRegistry robotRegistry;
  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @Test
  void startScheduling_shouldRegisterFixedRateScheduler() {
    AdvanceRobotStateScheduler scheduler = new AdvanceRobotStateScheduler(
        robotTelemetryScheduler,
        simulatorProperties,
        robotRegistry,
        applicationEventPublisher);
    when(simulatorProperties.getStateAdvanceIntervalMs()).thenReturn(1000L);

    scheduler.startScheduling();

    verify(robotTelemetryScheduler).scheduleAtFixedRate(
        any(Runnable.class),
        eq(0L),
        eq(1000L),
        eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void scheduledRunnable_shouldPublishAdvanceEventForEachRobot() {
    AdvanceRobotStateScheduler scheduler = new AdvanceRobotStateScheduler(
        robotTelemetryScheduler,
        simulatorProperties,
        robotRegistry,
        applicationEventPublisher);
    when(simulatorProperties.getStateAdvanceIntervalMs()).thenReturn(500L);

    RobotActor robot1 = RobotActor.builder()
        .robotId("robot-1")
        .positionX(1.0)
        .positionY(2.0)
        .battery(50.0)
        .status(RobotStatus.IDLE)
        .build();
    RobotActor robot2 = RobotActor.builder()
        .robotId("robot-2")
        .positionX(3.0)
        .positionY(4.0)
        .battery(60.0)
        .status(RobotStatus.MOVING)
        .build();
    when(robotRegistry.getRegisteredRobots()).thenReturn(List.of(robot1, robot2));

    scheduler.startScheduling();

    ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(robotTelemetryScheduler).scheduleAtFixedRate(
        runnableCaptor.capture(),
        eq(0L),
        eq(500L),
        eq(TimeUnit.MILLISECONDS));

    runnableCaptor.getValue().run();

    verify(applicationEventPublisher).publishEvent(argThat(
        hasRequestFor(robot1)));
    verify(applicationEventPublisher).publishEvent(argThat(
        hasRequestFor(robot2)));
  }

  private ArgumentMatcher<AdvanceRobotStateRequest> hasRequestFor(RobotActor expectedRobotActor) {
    return request -> request != null
        && request.robotActor() == expectedRobotActor
        && request.advancementMode() instanceof AdvancementMode;
  }
}
