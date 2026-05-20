package com.robofleet.robotsimulator.robot.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.robofleet.robotsimulator.config.SimulatorProperties;
import com.robofleet.robotsimulator.robot.domain.RobotRegistry;
import com.robofleet.robotsimulator.robot.domain.map.RectangularMap;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class RobotSimulationOrchestratorTest {

  @Mock
  private SimulatorProperties simulatorProperties;
  @Mock
  private RobotRegistry robotRegistry;
  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @Test
  void startFleet_shouldRegisterRobotsAndPublishEvents() {
    RobotMap robotMap = new RectangularMap(0.0, 100.0, 0.0, 100.0);
    RobotSimulationOrchestrator robotSimulationOrchestrator = new RobotSimulationOrchestrator(
        simulatorProperties,
        robotMap,
        robotRegistry,
        applicationEventPublisher);

    when(simulatorProperties.getRobotCount()).thenReturn(3);
    when(simulatorProperties.getTelemetryIntervalMs()).thenReturn(1000L);

    robotSimulationOrchestrator.startFleet();

    verify(robotRegistry, times(3)).register(any());
    verify(applicationEventPublisher, times(3)).publishEvent(any(RobotTelemetryInstrumentationRequestedEvent.class));
    verify(applicationEventPublisher, times(3)).publishEvent(any(RobotStateAdvancementRequestedEvent.class));
  }
}
