package com.robofleet.robotsimulator.robot.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.robofleet.robotsimulator.config.SimulatorProperties;
import com.robofleet.robotsimulator.robot.application.actor.RobotActorRef;
import com.robofleet.robotsimulator.robot.application.actor.RobotOrchestration;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdvanceRobotStateSchedulerTest {

  @Mock
  private ScheduledExecutorService robotTelemetryScheduler;
  @Mock
  private SimulatorProperties simulatorProperties;
  @Mock
  private RobotOrchestrator robotOrchestrator;
  @Mock
  private RobotActorRef robotActorRefOne;
  @Mock
  private RobotActorRef robotActorRefTwo;

  @Test
  void startScheduling_shouldRegisterFixedRateScheduler() {
    AdvanceRobotStateScheduler scheduler = new AdvanceRobotStateScheduler(
        robotTelemetryScheduler,
        simulatorProperties,
        robotOrchestrator);
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
        robotOrchestrator);
    when(simulatorProperties.getStateAdvanceIntervalMs()).thenReturn(500L);
    doAnswer(invocation -> {
      Object message = invocation.getArgument(0);
      if (message instanceof RobotOrchestration.TellAll(var rc)) {
        robotActorRefOne.tell(rc);
        robotActorRefTwo.tell(rc);
      }
      return null;
    }).when(robotOrchestrator).tell(any());

    scheduler.startScheduling();

    ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(robotTelemetryScheduler).scheduleAtFixedRate(
        runnableCaptor.capture(),
        eq(0L),
        eq(500L),
        eq(TimeUnit.MILLISECONDS));

    runnableCaptor.getValue().run();

    verify(robotActorRefOne).tell(any());
    verify(robotActorRefTwo).tell(any());
  }
}
