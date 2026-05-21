package com.robofleet.fleetstateservice.robot.application.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Command payload to request creation of a simulator robot.
 */
@Value
@Builder
@Jacksonized
public class CreateRobotRequest {
  @Builder.Default
  String correlationId = UUID.randomUUID().toString();
  String displayName;
}
