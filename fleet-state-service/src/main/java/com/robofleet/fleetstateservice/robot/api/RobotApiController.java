package com.robofleet.fleetstateservice.robot.api;

import com.robofleet.fleetstateservice.robot.application.RobotStateService;
import com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-facing API for current fleet state.
 *
 * <p>Intent: expose a clean, stable query surface for operator experiences
 * (dashboards, status widgets, ad-hoc checks) without coupling them to Kafka
 * or persistence implementation details.</p>
 */
@RestController
@RequestMapping("/api/robots")
@RequiredArgsConstructor
public class RobotApiController {

  private final RobotStateService robotStateService;

  /**
   * Lists the latest known state for all robots.
   *
   * @param pageable pagination and sorting request
   * @return current fleet snapshot
   */
  @GetMapping
  public List<RobotStateResponse> getAllRobots(
      @PageableDefault(size = 100, sort = "robotId", direction = Sort.Direction.ASC)
      Pageable pageable) {
    return robotStateService.getAllRobots(pageable);
  }

  /**
   * Fetches the latest known state for a specific robot.
   *
   * @param id robot identifier
   * @return 200 with state when found, otherwise 404
   */
  @GetMapping("/{id}")
  public ResponseEntity<RobotStateResponse> getRobotById(@PathVariable("id") String id) {
    return robotStateService.getRobotById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
