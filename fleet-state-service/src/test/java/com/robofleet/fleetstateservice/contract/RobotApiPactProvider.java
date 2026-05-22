package com.robofleet.fleetstateservice.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Consumer;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.robofleet.fleetstateservice.robot.api.RobotApiController;
import com.robofleet.fleetstateservice.robot.application.RobotStateService;
import com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse;
import com.robofleet.fleetstateservice.robot.application.dto.RobotSummaryResponse;
import com.robofleet.fleetstateservice.robot.domain.RobotLifecycleStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Provider-side Pact verification for HTTP contracts exposed by {@code fleet-state-service} to
 * the dashboard consumer.
 *
 * <p>This class binds Pact verification to a MockMvc target and provides state fixtures through
 * mocked {@link RobotStateService} responses.
 *
 * @author Nilabro Saha
 */
@WebMvcTest(RobotApiController.class)
@Provider("fleet-state-service")
@Consumer("fleet-dashboard-ui")
@PactFolder("../pacts")
class RobotApiPactProvider {

  @Autowired
  private RobotApiController robotApiController;

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RobotStateService robotStateService;

  /** Configures the Pact verification target to use Spring MockMvc. */
  @BeforeEach
  void before(PactVerificationContext context) {
    if (context != null) {
      MockMvcTestTarget target = new MockMvcTestTarget();
      target.setMockMvc(mockMvc);
      context.setTarget(target);
    }
  }

  /** Executes each Pact interaction discovered for this provider/consumer pair. */
  @TestTemplate
  @ExtendWith(PactVerificationInvocationContextProvider.class)
  void verifyPact(PactVerificationContext context) {
    context.verifyInteraction();
  }

  /** Supplies provider state for {@code GET /api/robot-statuses}. */
  @State("robot statuses exist")
  void robotStatusesExist() {
    when(robotStateService.getAllRobotStatuses(any(Pageable.class))).thenReturn(List.of(
        RobotStateResponse.builder()
            .robotId("robot-1")
            .displayName("Alpha")
            .positionX(12.34)
            .positionY(56.78)
            .battery(87.1)
            .lifecycleStatus(RobotLifecycleStatus.ACTIVE)
            .status("MOVING")
            .timestamp(Instant.parse("2026-05-19T16:40:03Z"))
            .build()));
  }

  /** Supplies provider state for {@code GET /api/robots}. */
  @State("robot summaries exist")
  void robotSummariesExist() {
    when(robotStateService.getAllRobots(any(Pageable.class))).thenReturn(List.of(
        RobotSummaryResponse.builder()
            .robotId("robot-1")
            .displayName("Alpha")
            .lifecycleStatus(RobotLifecycleStatus.ACTIVE)
            .build()));
  }

  /** Supplies provider state for {@code GET /api/robots/{id}} using {@code robot-1}. */
  @State("robot with id robot-1 exists")
  void robotWithIdExists() {
    when(robotStateService.getRobotById("robot-1")).thenReturn(Optional.of(
        RobotSummaryResponse.builder()
            .robotId("robot-1")
            .displayName("Alpha")
            .lifecycleStatus(RobotLifecycleStatus.ACTIVE)
            .build()));
  }
}
