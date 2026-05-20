package com.robofleet.robotsimulator.robot.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.robofleet.robotsimulator.config.SimulatorProperties;
import com.robofleet.robotsimulator.robot.domain.RobotActor;
import com.robofleet.robotsimulator.robot.domain.RobotStatus;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RobotTelemetryInstrumentationListenerTest {

  @Mock
  private ScheduledExecutorService robotTelemetryScheduler;
  @Mock
  private SimulatorProperties simulatorProperties;
  @Mock
  private RobotTelemetryPublisher robotTelemetryPublisher;

  @InjectMocks
  private RobotTelemetryInstrumentationListener listener;

  @Test
  void onRobotTelemetryInstrumentationRequested_shouldScheduleTelemetryPublishing() {
    RobotActor robotActor = RobotActor.builder()
        .robotId("robot-1")
        .positionX(10.0)
        .positionY(10.0)
        .battery(60.0)
        .status(RobotStatus.IDLE)
        .build();
    when(simulatorProperties.getTelemetryIntervalMs()).thenReturn(1000L);

    listener.onRobotTelemetryInstrumentationRequested(
        new RobotTelemetryInstrumentationRequestedEvent(robotActor));

    verify(robotTelemetryScheduler).scheduleAtFixedRate(
        any(Runnable.class),
        eq(0L),
        eq(1000L),
        eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void scheduledRunnable_shouldAttemptTelemetryPublish() {
    RobotActor robotActor = RobotActor.builder()
        .robotId("robot-2")
        .positionX(1.0)
        .positionY(2.0)
        .battery(30.0)
        .status(RobotStatus.MOVING)
        .build();
    when(simulatorProperties.getTelemetryIntervalMs()).thenReturn(500L);

    listener.onRobotTelemetryInstrumentationRequested(
        new RobotTelemetryInstrumentationRequestedEvent(robotActor));

    ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(robotTelemetryScheduler).scheduleAtFixedRate(
        runnableCaptor.capture(),
        eq(0L),
        eq(500L),
        eq(TimeUnit.MILLISECONDS));

    runnableCaptor.getValue().run();

    verify(robotTelemetryPublisher).publish(any());
  }

  @Test
  void scheduledRunnable_shouldSwallowPublisherException() {
    RobotActor robotActor = RobotActor.builder()
        .robotId("robot-3")
        .positionX(1.0)
        .positionY(2.0)
        .battery(30.0)
        .status(RobotStatus.MOVING)
        .build();
    when(simulatorProperties.getTelemetryIntervalMs()).thenReturn(500L);
    doThrow(new RuntimeException("boom")).when(robotTelemetryPublisher).publish(any());

    listener.onRobotTelemetryInstrumentationRequested(
        new RobotTelemetryInstrumentationRequestedEvent(robotActor));

    ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(robotTelemetryScheduler).scheduleAtFixedRate(
        runnableCaptor.capture(),
        eq(0L),
        eq(500L),
        eq(TimeUnit.MILLISECONDS));

    runnableCaptor.getValue().run();

    verify(robotTelemetryPublisher).publish(any());
  }
}
