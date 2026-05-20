package com.robofleet.robotsimulator.robot.domain.behavior;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class AdvancementModeTypesTest {

  @Test
  void holdPositionAdvance_shouldImplementAdvancementMode() {
    AdvancementMode mode = new HoldPositionAdvance();
    assertTrue(mode instanceof HoldPositionAdvance);
  }

  @Test
  void pathFollowingAdvance_shouldImplementAdvancementMode() {
    AdvancementMode mode = new PathFollowingAdvance();
    assertTrue(mode instanceof PathFollowingAdvance);
  }

  @Test
  void randomAdvance_shouldRequireNonNullRandomGenerator() {
    assertThrows(NullPointerException.class, () -> new RandomAdvance(null));
  }

  @Test
  void randomAdvance_shouldExposeConfiguredRandomGenerator() {
    Random random = new Random(42);
    RandomAdvance mode = new RandomAdvance(random);
    assertTrue(mode.randomGenerator() == random);
  }
}
