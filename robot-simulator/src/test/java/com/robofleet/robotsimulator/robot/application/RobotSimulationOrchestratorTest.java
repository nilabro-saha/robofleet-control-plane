package com.robofleet.robotsimulator.robot.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.robofleet.robotsimulator.config.SimulatorProperties;
import com.robofleet.robotsimulator.robot.domain.model.RobotActor;
import com.robofleet.robotsimulator.robot.domain.model.RobotStatus;
import com.robofleet.robotsimulator.robot.domain.map.RectangularMap;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
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
    Assertions.assertTrue(
        robotCaptor.getAllValues().stream().allMatch(robot -> robot.getRobotId() != null));
  }

  @Test
  void registerRobot_shouldRegisterProvidedIdentity() {
    RobotMap robotMap = new RectangularMap(0.0, 100.0, 0.0, 100.0);
    RobotSimulationOrchestrator orchestrator = new RobotSimulationOrchestrator(
        simulatorProperties,
        robotMap,
        robotRegistry);

    orchestrator.registerRobot("robot-99");

    verify(robotRegistry, times(1)).register(any());

    ArgumentCaptor<RobotActor> robotCaptor = ArgumentCaptor.forClass(RobotActor.class);
    verify(robotRegistry).register(robotCaptor.capture());
    Assertions.assertEquals("robot-99", robotCaptor.getValue().getRobotId());
  }

  @Test
  void registerRobot_shouldUseProvidedStateWhenAvailable() {
    RobotMap robotMap = new RectangularMap(0.0, 100.0, 0.0, 100.0);
    RobotSimulationOrchestrator orchestrator = new RobotSimulationOrchestrator(
        simulatorProperties,
        robotMap,
        robotRegistry);

    orchestrator.registerRobot("robot-11", 12.5, 9.75, 88.0, "MOVING");

    ArgumentCaptor<RobotActor> robotCaptor = ArgumentCaptor.forClass(RobotActor.class);
    verify(robotRegistry).register(robotCaptor.capture());
    RobotActor robot = robotCaptor.getValue();
    Assertions.assertEquals("robot-11", robot.getRobotId());
    Assertions.assertEquals(12.5, robot.getPositionX());
    Assertions.assertEquals(9.75, robot.getPositionY());
    Assertions.assertEquals(88.0, robot.getBattery());
    Assertions.assertEquals(RobotStatus.MOVING, robot.getStatus());
  }

  @Test
  void registerRobot_shouldSkipWhenAlreadyRegistered() {
    RobotMap robotMap = new RectangularMap(0.0, 100.0, 0.0, 100.0);
    RobotSimulationOrchestrator orchestrator = new RobotSimulationOrchestrator(
        simulatorProperties,
        robotMap,
        robotRegistry);
    when(robotRegistry.getRegisteredRobots()).thenReturn(List.of(
        RobotActor.builder()
            .robotId("robot-99")
            .positionX(1.0)
            .positionY(1.0)
            .battery(80.0)
            .status(RobotStatus.IDLE)
            .build()
    ));

    orchestrator.registerRobot("robot-99");

    verify(robotRegistry, times(0)).register(any());
  }

  @Test
  void deregisterRobot_shouldDelegateToRegistry() {
    RobotMap robotMap = new RectangularMap(0.0, 100.0, 0.0, 100.0);
    RobotSimulationOrchestrator orchestrator = new RobotSimulationOrchestrator(
        simulatorProperties,
        robotMap,
        robotRegistry);
    when(robotRegistry.deregisterById("robot-99")).thenReturn(true);

    orchestrator.deregisterRobot("robot-99");

    verify(robotRegistry, times(1)).deregisterById("robot-99");
  }

  @Test
  void deregisterRobot_shouldNoOpWhenRobotMissing() {
    RobotMap robotMap = new RectangularMap(0.0, 100.0, 0.0, 100.0);
    RobotSimulationOrchestrator orchestrator = new RobotSimulationOrchestrator(
        simulatorProperties,
        robotMap,
        robotRegistry);
    when(robotRegistry.deregisterById("missing")).thenReturn(false);

    orchestrator.deregisterRobot("missing");

    verify(robotRegistry, times(1)).deregisterById("missing");
  }
}
