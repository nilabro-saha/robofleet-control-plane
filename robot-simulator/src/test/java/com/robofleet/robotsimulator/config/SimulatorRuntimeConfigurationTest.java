package com.robofleet.robotsimulator.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.robofleet.robotsimulator.robot.application.RobotOrchestrator;
import com.robofleet.robotsimulator.robot.domain.map.RectangularMap;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import java.util.concurrent.ScheduledExecutorService;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class SimulatorRuntimeConfigurationTest {

  private final SimulatorRuntimeConfiguration configuration = new SimulatorRuntimeConfiguration();

  @Test
  void robotTelemetryScheduler_shouldCreateSchedulerBean() {
    ScheduledExecutorService scheduler = configuration.robotTelemetryScheduler();

    assertNotNull(scheduler);
    scheduler.shutdownNow();
  }

  @Test
  void robotMap_shouldCreateRectangularMapFromProperties() {
    SimulatorProperties properties = new SimulatorProperties();
    properties.setMapMinX(1.0);
    properties.setMapMaxX(11.0);
    properties.setMapMinY(2.0);
    properties.setMapMaxY(12.0);

    RobotMap robotMap = configuration.robotMap(properties);

    RectangularMap rectangularMap = assertInstanceOf(RectangularMap.class, robotMap);
    assertEquals(1.0, rectangularMap.minX());
    assertEquals(11.0, rectangularMap.maxX());
    assertEquals(2.0, rectangularMap.minY());
    assertEquals(12.0, rectangularMap.maxY());
  }

  @Test
  void robotOrchestrator_shouldCreateOrchestratorBean() {
    RobotOrchestrator robotOrchestrator = configuration.robotOrchestrator(
        new RectangularMap(0.0, 100.0, 0.0, 100.0),
        mock(ApplicationEventPublisher.class)
    );

    assertNotNull(robotOrchestrator);
  }

}
