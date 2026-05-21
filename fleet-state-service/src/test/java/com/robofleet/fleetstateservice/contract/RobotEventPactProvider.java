package com.robofleet.fleetstateservice.contract;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Consumer;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.robofleet.fleetstateservice.robot.infrastructure.messaging.LifecycleEventType;
import com.robofleet.fleetstateservice.robot.infrastructure.messaging.RobotLifecycleEvent;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@Provider("fleet-state-service")
@Consumer("robot-simulator")
@PactFolder("../pacts")
class RobotEventPactProvider {

  private final ObjectMapper objectMapper = new ObjectMapper()
      .findAndRegisterModules()
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  @BeforeEach
  @SuppressWarnings("JUnitMalformedDeclaration")
  void before(PactVerificationContext context) {
    if (context != null) {
      context.setTarget(new MessageTestTarget());
    }
  }

  @TestTemplate
  @ExtendWith(PactVerificationInvocationContextProvider.class)
  void verifyPact(PactVerificationContext context) {
    context.verifyInteraction();
  }

  @State("create pending lifecycle event is emitted")
  void createPendingLifecycleEventIsEmitted() {
    // static fixture
  }

  @State("delete pending lifecycle event is emitted")
  void deletePendingLifecycleEventIsEmitted() {
    // static fixture
  }

  @PactVerifyProvider("create pending lifecycle event")
  String createPendingLifecycleEvent() throws JsonProcessingException {
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .eventName("robot-lifecycle-changed")
        .correlationId("corr-create-1")
        .robotId("robot-1")
        .positionX(12.34)
        .positionY(56.78)
        .battery(87.1)
        .status("IDLE")
        .eventType(LifecycleEventType.CREATE_PENDING)
        .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
        .build();
    return objectMapper.writeValueAsString(event);
  }

  @PactVerifyProvider("delete pending lifecycle event")
  String deletePendingLifecycleEvent() throws JsonProcessingException {
    RobotLifecycleEvent event = RobotLifecycleEvent.builder()
        .eventName("robot-lifecycle-changed")
        .correlationId("corr-delete-1")
        .robotId("robot-1")
        .positionX(12.34)
        .positionY(56.78)
        .battery(87.1)
        .status("IDLE")
        .eventType(LifecycleEventType.DELETE_PENDING)
        .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
        .build();
    return objectMapper.writeValueAsString(event);
  }
}
