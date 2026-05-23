package com.robofleet.robotsimulator.robot.application.actor;

import com.robofleet.robotsimulator.robot.domain.model.RobotState;

/**
 * Marker contract for commands handled by {@code RobotOrchestrator} via message passing.
 *
 * @author Nilabro Saha
 */
public sealed interface RobotOrchestration permits
    RobotOrchestration.Spawn,
    RobotOrchestration.SpawnRandom,
    RobotOrchestration.Destroy,
    RobotOrchestration.DestroyAll,
    RobotOrchestration.TellAll {

  /**
   * Spawns one robot from explicit state.
   */
  record Spawn(RobotState requestedState, String correlationId) implements RobotOrchestration {
  }

  /**
   * Spawns one robot with randomized state.
   */
  record SpawnRandom(String robotId, String correlationId) implements RobotOrchestration {
  }

  /**
   * Destroys one robot.
   */
  record Destroy(String robotId, String correlationId) implements RobotOrchestration {
  }

  /**
   * Destroys all robots.
   */
  record DestroyAll() implements RobotOrchestration {
  }

  /**
   * Broadcasts one robot command to all robots.
   */
  record TellAll(RobotCommand robotCommand) implements RobotOrchestration {
  }
}