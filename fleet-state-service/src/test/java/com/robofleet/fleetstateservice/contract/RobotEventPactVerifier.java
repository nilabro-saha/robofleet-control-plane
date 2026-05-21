package com.robofleet.fleetstateservice.contract;

import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.V4Interaction;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;
import au.com.dius.pact.core.model.annotations.Pact;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * Consumer-side Pact contracts for Kafka-style robot events consumed by fleet-state-service.
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "robot-simulator", providerType = ProviderType.ASYNCH)
@PactDirectory("../pacts")
class RobotEventPactVerifier {

  @Pact(consumer = "fleet-state-service")
  V4Pact telemetryEventPact(PactBuilder builder) {
    return builder
        .usingLegacyMessageDsl()
        .given("robot telemetry event is emitted")
        .expectsToReceive("robot telemetry event")
        .withMetadata(Map.of("contentType", "application/json"))
        .withContent(
            LambdaDsl.newJsonBody(obj -> {
              obj.stringMatcher("schemaVersion", "v\\d+", "v1");
              obj.stringType("eventName", "robot-state-changed");
              obj.stringType("correlationId", "corr-1");
              obj.stringType("robotId", "robot-1");
              obj.numberType("x", 12.34);
              obj.numberType("y", 56.78);
              obj.numberType("battery", 87.1);
              obj.stringType("status", "MOVING");
              obj.stringMatcher(
                  "timestamp",
                  "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z",
                  "2026-05-19T16:40:03Z");
            }).build())
        .toPact(V4Pact.class);
  }

  @Pact(consumer = "fleet-state-service")
  V4Pact lifecycleEventPact(PactBuilder builder) {
    return builder
        .usingLegacyMessageDsl()
        .given("robot lifecycle event is emitted")
        .expectsToReceive("robot lifecycle event")
        .withMetadata(Map.of("contentType", "application/json"))
        .withContent(
            LambdaDsl.newJsonBody(obj -> {
              obj.stringMatcher("schemaVersion", "v\\d+", "v1");
              obj.stringType("eventName", "robot-lifecycle-changed");
              obj.stringType("correlationId", "corr-1");
              obj.stringType("robotId", "robot-1");
              obj.stringMatcher("eventType", "[A-Z_]+", "CREATED");
              obj.numberType("x", 12.34);
              obj.numberType("y", 56.78);
              obj.numberType("battery", 87.1);
              obj.stringType("status", "IDLE");
              obj.stringMatcher(
                  "timestamp",
                  "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z",
                  "2026-05-19T16:40:03Z");
            }).build())
        .toPact(V4Pact.class);
  }

  @Test
  @PactTestFor(pactMethod = "telemetryEventPact", providerType = ProviderType.ASYNCH)
  void shouldMatchTelemetryEventContract(V4Interaction.AsynchronousMessage message) throws Exception {
    String json = message.contentsAsString();
    JSONAssert.assertEquals(
        """
            {
              "schemaVersion": "v1",
              "eventName": "robot-state-changed",
              "robotId": "robot-1"
            }
            """,
        json,
        false);
  }

  @Test
  @PactTestFor(pactMethod = "lifecycleEventPact", providerType = ProviderType.ASYNCH)
  void shouldMatchLifecycleEventContract(V4Interaction.AsynchronousMessage message) throws Exception {
    String json = message.contentsAsString();
    JSONAssert.assertEquals(
        """
            {
              "schemaVersion": "v1",
              "eventName": "robot-lifecycle-changed",
              "eventType": "CREATED"
            }
            """,
        json,
        false);
  }
}
