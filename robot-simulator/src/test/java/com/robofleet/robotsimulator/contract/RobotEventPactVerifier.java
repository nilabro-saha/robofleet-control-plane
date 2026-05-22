package com.robofleet.robotsimulator.contract;

import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.V4Interaction;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * Consumer-side Pact contracts for asynchronous lifecycle command events consumed by
 * {@code robot-simulator}.
 *
 * <p>These contracts assert the payload shape expected from fleet-state-service when issuing
 * create/delete lifecycle commands.
 *
 * @author Nilabro Saha
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "fleet-state-service", providerType = ProviderType.ASYNCH)
@PactDirectory("../pacts")
class RobotEventPactVerifier {

  /** Defines the contract for the create-pending lifecycle command message. */
  @Pact(consumer = "robot-simulator")
  V4Pact createPendingLifecycleCommandPact(PactBuilder builder) {
    return builder
        .usingLegacyMessageDsl()
        .given("create pending lifecycle event is emitted")
        .expectsToReceive("create pending lifecycle event")
        .withMetadata(Map.of("contentType", "application/json"))
        .withContent(
            LambdaDsl.newJsonBody(obj -> {
              obj.stringMatcher("schemaVersion", "v\\d+", "v1");
              obj.stringType("eventName", "robot-lifecycle-changed");
              obj.stringType("correlationId", "corr-create-1");
              obj.stringType("robotId", "robot-1");
              obj.numberType("x", 12.34);
              obj.numberType("y", 56.78);
              obj.numberType("battery", 87.1);
              obj.stringType("status", "IDLE");
              obj.stringMatcher("eventType", "[A-Z_]+", "CREATE_PENDING");
              obj.stringMatcher(
                  "timestamp",
                  "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z",
                  "2026-05-19T16:40:03Z");
            }).build())
        .toPact(V4Pact.class);
  }

  /** Defines the contract for the delete-pending lifecycle command message. */
  @Pact(consumer = "robot-simulator")
  V4Pact deletePendingLifecycleCommandPact(PactBuilder builder) {
    return builder
        .usingLegacyMessageDsl()
        .given("delete pending lifecycle event is emitted")
        .expectsToReceive("delete pending lifecycle event")
        .withMetadata(Map.of("contentType", "application/json"))
        .withContent(
            LambdaDsl.newJsonBody(obj -> {
              obj.stringMatcher("schemaVersion", "v\\d+", "v1");
              obj.stringType("eventName", "robot-lifecycle-changed");
              obj.stringType("correlationId", "corr-delete-1");
              obj.stringType("robotId", "robot-1");
              obj.numberType("x", 12.34);
              obj.numberType("y", 56.78);
              obj.numberType("battery", 87.1);
              obj.stringType("status", "IDLE");
              obj.stringMatcher("eventType", "[A-Z_]+", "DELETE_PENDING");
              obj.stringMatcher(
                  "timestamp",
                  "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z",
                  "2026-05-19T16:40:03Z");
            }).build())
        .toPact(V4Pact.class);
  }

  /** Verifies the generated create-pending lifecycle command message fixture. */
  @Test
  @PactTestFor(pactMethod = "createPendingLifecycleCommandPact", providerType = ProviderType.ASYNCH)
  void shouldMatchCreatePendingLifecycleCommand(V4Interaction.AsynchronousMessage message)
      throws Exception {
    String json = message.contentsAsString();
    JSONAssert.assertEquals(
        """
            {
              "eventName": "robot-lifecycle-changed",
              "eventType": "CREATE_PENDING"
            }
            """,
        json,
        false);
  }

  /** Verifies the generated delete-pending lifecycle command message fixture. */
  @Test
  @PactTestFor(pactMethod = "deletePendingLifecycleCommandPact", providerType = ProviderType.ASYNCH)
  void shouldMatchDeletePendingLifecycleCommand(V4Interaction.AsynchronousMessage message)
      throws Exception {
    String json = message.contentsAsString();
    JSONAssert.assertEquals(
        """
            {
              "eventName": "robot-lifecycle-changed",
              "eventType": "DELETE_PENDING"
            }
            """,
        json,
        false);
  }
}
