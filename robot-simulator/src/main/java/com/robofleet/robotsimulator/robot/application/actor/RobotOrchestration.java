package com.robofleet.robotsimulator.robot.application.actor;

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
  record Spawn(RobotState requestedState) implements RobotOrchestration {
  }

  /**
   * Spawns one robot with randomized state.
   */
  record SpawnRandom(String robotId) implements RobotOrchestration {
  }

  /**
   * Destroys one robot.
   */
  record Destroy(String robotId) implements RobotOrchestration {
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