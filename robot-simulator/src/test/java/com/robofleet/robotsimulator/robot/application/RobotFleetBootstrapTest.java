package com.robofleet.robotsimulator.robot.application;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RobotFleetBootstrapTest {

  @Mock
  private RobotSimulationOrchestrator robotSimulationOrchestrator;

  @InjectMocks
  private RobotFleetBootstrap robotFleetBootstrap;

  @Test
  void bootstrapFleet_shouldDelegateToOrchestrator() {
    robotFleetBootstrap.bootstrapFleet();

    verify(robotSimulationOrchestrator).startFleet();
  }
}
