package com.robofleet.robotsimulator.robot.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.robofleet.robotsimulator.config.SimulatorProperties;
import com.robofleet.robotsimulator.robot.domain.model.RobotActor;
import com.robofleet.robotsimulator.robot.domain.model.RobotStatus;
import com.robofleet.robotsimulator.robot.domain.map.RectangularMap;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class RobotSimulationOrchestratorTest {

  @Mock
  private SimulatorProperties simulatorProperties;
  @Mock
  private RobotRegistry robotRegistry;

  @Test
  void startFleet_shouldRegisterRobots() {
    RobotMap robotMap = new RectangularMap(0.0, 100.0, 0.0, 100.0);
    RobotSimulationOrchestrator robotSimulationOrchestrator = new RobotSimulationOrchestrator(
        simulatorProperties,
        robotMap,
        robotRegistry);

    when(simulatorProperties.getRobotCount()).thenReturn(3);
    when(simulatorProperties.getTelemetryIntervalMs()).thenReturn(1000L);
    when(robotRegistry.clearAndGetRemovedRobots()).thenReturn(List.of(
        RobotActor.builder()
            .robotId("old-1")
            .positionX(0.0)
            .positionY(0.0)
            .battery(0.0)
            .status(RobotStatus.IDLE)
            .build(),
        RobotActor.builder()
            .robotId("old-2")
            .positionX(0.0)
            .positionY(0.0)
            .battery(0.0)
            .status(RobotStatus.IDLE)
            .build()
    ));

    robotSimulationOrchestrator.startFleet();

    verify(robotRegistry).clearAndGetRemovedRobots();
    verify(robotRegistry, times(3)).register(any());

    ArgumentCaptor<RobotActor> robotCaptor = ArgumentCaptor.forClass(RobotActor.class);
    verify(robotRegistry, times(3)).register(robotCaptor.capture());
    assertTrue(robotCaptor.getAllValues().stream()
        .anyMatch(robot -> robot.getDisplayName() == null || robot.getDisplayName().startsWith("Robot-")));
  }
}
