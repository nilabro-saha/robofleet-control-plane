package com.robofleet.robotsimulator.config;

import com.robofleet.robotsimulator.robot.application.RobotRegistry;
import com.robofleet.robotsimulator.robot.domain.map.RectangularMap;
import com.robofleet.robotsimulator.robot.domain.map.RobotMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Runtime bean wiring for registry and shared scheduling pool.
 *
 * <p>Intent: centralize simulator runtime composition so application services depend on stable,
 * testable abstractions instead of constructing infrastructure directly.</p>
 *
 * @author Nilabro Saha
 */
@Configuration
public class SimulatorRuntimeConfiguration {

  /**
   * Shared scheduled pool where robot telemetry tasks are registered.
   *
   * @return shared scheduler used by simulator background tasks
   */
  @Bean(destroyMethod = "shutdownNow")
  public ScheduledExecutorService robotTelemetryScheduler() {
    return Executors.newScheduledThreadPool(8);
  }

  /**
   * Bounded rectangular map configured for robot simulation.
   *
   * @param simulatorProperties simulator map-boundary properties
   * @return map implementation used by robot movement logic
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
   *
   * @param applicationEventPublisher event publisher for robot lifecycle events
   * @return runtime robot registry bean
   */
  @Bean
  public RobotRegistry robotRegistry(ApplicationEventPublisher applicationEventPublisher) {
    return new RobotRegistry(applicationEventPublisher);
  }
}
