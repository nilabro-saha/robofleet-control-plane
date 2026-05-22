package com.robofleet.robotsimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for the Spring-based robot simulator module.
 *
 * <p>Intent: bootstrap the simulator runtime that models robots as in-memory actors and emits
 * telemetry/lifecycle events for downstream control-plane services.</p>
 *
 * @author Nilabro Saha
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class RobotSimulatorApplication {

  /**
   * Boots the Robot Simulator Spring application context.
   *
   * @param args standard Spring Boot startup arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(RobotSimulatorApplication.class, args);
  }
}
