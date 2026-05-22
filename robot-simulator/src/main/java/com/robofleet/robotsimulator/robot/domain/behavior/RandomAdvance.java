package com.robofleet.robotsimulator.robot.domain.behavior;

import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Randomized state-advancement strategy.
 *
 * @param randomGenerator source of pseudo-random values used for state transitions
 * @author Nilabro Saha
 */
public record RandomAdvance(RandomGenerator randomGenerator) implements AdvancementMode {

  public RandomAdvance {
    Objects.requireNonNull(randomGenerator, "randomGenerator cannot be null");
  }
}
