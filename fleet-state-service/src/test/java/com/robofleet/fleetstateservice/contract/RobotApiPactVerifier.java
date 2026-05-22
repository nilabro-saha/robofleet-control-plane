package com.robofleet.fleetstateservice.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * Consumer-side Pact contract tests for the dashboard-facing HTTP APIs exposed by
 * {@code fleet-state-service}.
 *
 * <p>These tests define the expected request/response shape and assert that generated Pact
 * interactions remain compatible with the dashboard consumer expectations.
 *
 * @author Nilabro Saha
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "fleet-state-service")
@PactDirectory("../pacts")
class RobotApiPactVerifier {

  private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

  /**
   * Defines the contract for listing robot status projections via {@code GET /api/robot-statuses}.
   */
  @Pact(consumer = "fleet-dashboard-ui")
  V4Pact getRobotStatusesPact(PactBuilder builder) {
    return builder
        .usingLegacyDsl()
        .given("robot statuses exist")
        .uponReceiving("a request for robot statuses")
        .path("/api/robot-statuses")
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            LambdaDsl.newJsonArrayMinLike(1, array -> array.object(obj -> {
              obj.stringType("robotId", "robot-1");
              obj.stringType("displayName", "Alpha");
              obj.numberType("x", 12.34);
              obj.numberType("y", 56.78);
              obj.numberType("battery", 87.1);
              obj.stringMatcher("lifecycleStatus", "[A-Z_]+", "ACTIVE");
              obj.stringType("status", "MOVING");
              obj.stringMatcher(
                  "timestamp",
                  "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z",
                  "2026-05-19T16:40:03Z");
            })).build())
        .toPact(V4Pact.class);
  }

  /**
   * Defines the contract for listing robot lifecycle summaries via {@code GET /api/robots}.
   */
  @Pact(consumer = "fleet-dashboard-ui")
  V4Pact getRobotsPact(PactBuilder builder) {
    return builder
        .usingLegacyDsl()
        .given("robot summaries exist")
        .uponReceiving("a request for robot summaries")
        .path("/api/robots")
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            LambdaDsl.newJsonArrayMinLike(1, array -> array.object(obj -> {
              obj.stringType("robotId", "robot-1");
              obj.stringType("displayName", "Alpha");
              obj.stringMatcher("lifecycleStatus", "[A-Z_]+", "ACTIVE");
            })).build())
        .toPact(V4Pact.class);
  }

  /**
   * Defines the contract for fetching a single robot summary via {@code GET /api/robots/{id}}.
   */
  @Pact(consumer = "fleet-dashboard-ui")
  V4Pact getRobotByIdPact(PactBuilder builder) {
    return builder
        .usingLegacyDsl()
        .given("robot with id robot-1 exists")
        .uponReceiving("a request for one robot summary")
        .path("/api/robots/robot-1")
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            LambdaDsl.newJsonBody(obj -> {
              obj.stringType("robotId", "robot-1");
              obj.stringType("displayName", "Alpha");
              obj.stringMatcher("lifecycleStatus", "[A-Z_]+", "ACTIVE");
            }).build())
        .toPact(V4Pact.class);
  }

  /**
   * Verifies that the generated interaction for {@code /api/robot-statuses} matches expectations.
   */
  @Test
  @PactTestFor(pactMethod = "getRobotStatusesPact")
  void shouldMatchGetRobotStatusesContract(MockServer mockServer)
      throws IOException, InterruptedException, Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(mockServer.getUrl() + "/api/robot-statuses"))
        .GET()
        .build();
    HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());
    JSONAssert.assertEquals(
        """
            [
              {
                "robotId": "robot-1"
              }
            ]
            """,
        response.body(),
        false);
  }

  /**
   * Verifies that the generated interaction for {@code /api/robots} matches expectations.
   */
  @Test
  @PactTestFor(pactMethod = "getRobotsPact")
  void shouldMatchGetRobotsContract(MockServer mockServer)
      throws IOException, InterruptedException, Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(mockServer.getUrl() + "/api/robots"))
        .GET()
        .build();
    HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());
    JSONAssert.assertEquals(
        """
            [
              {
                "lifecycleStatus": "ACTIVE"
              }
            ]
            """,
        response.body(),
        false);
  }

  /**
   * Verifies that the generated interaction for {@code /api/robots/{id}} matches expectations.
   */
  @Test
  @PactTestFor(pactMethod = "getRobotByIdPact")
  void shouldMatchGetRobotByIdContract(MockServer mockServer)
      throws IOException, InterruptedException, Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(mockServer.getUrl() + "/api/robots/robot-1"))
        .GET()
        .build();
    HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());
    JSONAssert.assertEquals(
        """
            {
              "displayName": "Alpha"
            }
            """,
        response.body(),
        false);
  }
}
