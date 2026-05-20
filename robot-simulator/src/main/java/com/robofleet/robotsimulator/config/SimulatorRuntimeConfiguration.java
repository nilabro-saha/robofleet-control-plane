package com.robofleet.robotsimulator.config;

import com.robofleet.robotsimulator.robot.domain.RobotRegistry;
import com.robofleet.robotsimulator.robot.domain.map.RectangularMap;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Runtime bean wiring for registry and shared scheduling pool.
 */
@Configuration
public class SimulatorRuntimeConfiguration {

  /**
   * Shared scheduled pool where robot telemetry tasks are registered.
   */
  @Bean(destroyMethod = "shutdownNow")
  public ScheduledExecutorService robotTelemetryScheduler() {
    return Executors.newScheduledThreadPool(8);
  }

  /**
   * Bounded rectangular map configured for robot simulation.
   */
  @Bean
  public RobotMap robotMap(SimulatorProperties simulatorProperties) {
    return new RectangularMap(
        simulatorProperties.getMapMinX(),
        simulatorProperties.getMapMaxX(),
        simulatorProperties.getMapMinY(),
        simulatorProperties.getMapMaxY()
    );
  }

  /**
   * Central robot registry for active robot actors.
   */
  @Bean
  public RobotRegistry robotRegistry() {
    return new RobotRegistry();
  }
}
