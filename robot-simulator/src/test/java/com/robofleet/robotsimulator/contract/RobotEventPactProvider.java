package com.robofleet.robotsimulator.contract;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.LifecycleEventType;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotLifecycleEvent;
import com.robofleet.robotsimulator.robot.infrastructure.messaging.event.RobotStateChangedEvent;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Provider-side Pact verification for asynchronous events emitted by {@code robot-simulator}.
 *
 * <p>This class exposes deterministic telemetry and lifecycle event fixtures used by
 * fleet-state-service consumer verification.
 *
 * @author Nilabro Saha
 */
@Provider("robot-simulator")
@PactFolder("../pacts")
class RobotEventPactProvider {

  private final ObjectMapper objectMapper = new ObjectMapper()
      .findAndRegisterModules()
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  /** Configures Pact to verify asynchronous message interactions. */
  @BeforeEach
  @SuppressWarnings("JUnitMalformedDeclaration")
  void before(PactVerificationContext context) {
    if (context != null) {
      context.setTarget(new MessageTestTarget());
    }
  }

  /** Executes each discovered Pact interaction for this provider. */
  @TestTemplate
  @ExtendWith(PactVerificationInvocationContextProvider.class)
  void verifyPact(PactVerificationContext context) {
    context.verifyInteraction();
  }

  /** Declares provider state for telemetry-event fixture generation. */
  @State("robot telemetry event is emitted")
  void robotTelemetryEventIsEmitted() {
    // State setup not required for static fixture payload.
  }

  /** Declares provider state for lifecycle-event fixture generation. */
  @State("robot lifecycle event is emitted")
  void robotLifecycleEventIsEmitted() {
    // State setup not required for static fixture payload.
  }

  /** Supplies the fixture payload for the telemetry-event interaction. */
  @PactVerifyProvider("robot telemetry event")
  String robotTelemetryEvent() throws JsonProcessingException {
    RobotStateChangedEvent event = RobotStateChangedEvent.builder()
        .eventName("robot-state-changed")
        .correlationId("corr-1")
        .robotId("robot-1")
        .positionX(12.34)
        .positionY(56.78)
        .battery(87.1)
        .status("MOVING")
        .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
        .build();
    return objectMapper.writeValueAsString(event);
  }

  /** Supplies the fixture payload for the lifecycle-event interaction. */
  @PactVerifyProvider("robot lifecycle event")
  String robotLifecycleEvent() throws JsonProcessingException {
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .eventName("robot-lifecycle-changed")
        .correlationId("corr-1")
        .robotId("robot-1")
        .positionX(12.34)
        .positionY(56.78)
        .battery(87.1)
        .status("IDLE")
        .eventType(LifecycleEventType.CREATED)
        .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
        .build();
    return objectMapper.writeValueAsString(event);
  }
}
