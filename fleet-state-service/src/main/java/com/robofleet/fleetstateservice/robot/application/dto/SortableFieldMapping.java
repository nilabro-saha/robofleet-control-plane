package com.robofleet.fleetstateservice.robot.application.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares backing entity sort metadata for a response DTO field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SortableFieldMapping {

  /**
   * Entity alias used by the repository query (for example: robot, state).
   */
  String entityAlias();

  /**
   * Entity attribute name used for sorting. If blank, the DTO field name is used.
   */
  String entityField() default "";
}
