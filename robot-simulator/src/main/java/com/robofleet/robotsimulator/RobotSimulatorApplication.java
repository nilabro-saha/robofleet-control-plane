package com.robofleet.robotsimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for the Spring-based robot simulator module.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class RobotSimulatorApplication {

  public static void main(String[] args) {
    SpringApplication.run(RobotSimulatorApplication.class, args);
  }
}
