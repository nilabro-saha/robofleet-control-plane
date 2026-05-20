package com.robofleet.robotsimulator.robot.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.robofleet.robotsimulator.config.SimulatorProperties;
import com.robofleet.robotsimulator.robot.domain.RobotActor;
import com.robofleet.robotsimulator.robot.domain.RobotStatus;
import com.robofleet.robotsimulator.robot.domain.map.RectangularMap;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RobotStateAdvancementListenerTest {

  @Mock
  private ScheduledExecutorService robotTelemetryScheduler;
  @Mock
  private SimulatorProperties simulatorProperties;

  @Test
  void onRobotStateAdvancementRequested_shouldScheduleAdvancement() {
    RobotMap robotMap = new RectangularMap(0.0, 100.0, 0.0, 100.0);
    RobotStateAdvancementListener listener = new RobotStateAdvancementListener(
        robotTelemetryScheduler,
        simulatorProperties,
        robotMap);

    RobotActor robotActor = RobotActor.builder()
        .robotId("robot-1")
        .positionX(10.0)
        .positionY(10.0)
        .battery(60.0)
        .status(RobotStatus.IDLE)
        .build();
    when(simulatorProperties.getStateAdvanceIntervalMs()).thenReturn(1000L);

    listener.onRobotStateAdvancementRequested(new RobotStateAdvancementRequestedEvent(robotActor));

    verify(robotTelemetryScheduler).scheduleAtFixedRate(
        any(Runnable.class),
        eq(0L),
        eq(1000L),
        eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void scheduledRunnable_shouldAdvanceRobotState() {
    RobotMap robotMap = new RectangularMap(0.0, 100.0, 0.0, 100.0);
    RobotStateAdvancementListener listener = new RobotStateAdvancementListener(
        robotTelemetryScheduler,
        simulatorProperties,
        robotMap);

    RobotActor robotActor = spy(RobotActor.builder()
        .robotId("robot-2")
        .positionX(90.0)
        .positionY(90.0)
        .battery(40.0)
        .status(RobotStatus.MOVING)
        .build());
    when(simulatorProperties.getStateAdvanceIntervalMs()).thenReturn(500L);
    doNothing().when(robotActor).advanceState(any());

    listener.onRobotStateAdvancementRequested(new RobotStateAdvancementRequestedEvent(robotActor));

    ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(robotTelemetryScheduler).scheduleAtFixedRate(
        runnableCaptor.capture(),
        eq(0L),
        eq(500L),
        eq(TimeUnit.MILLISECONDS));

    runnableCaptor.getValue().run();

    verify(robotActor).advanceState(robotMap);
  }
}
