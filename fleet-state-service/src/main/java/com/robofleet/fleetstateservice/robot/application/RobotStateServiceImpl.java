package com.robofleet.fleetstateservice.robot.application;

import com.robofleet.fleetstateservice.robot.application.dto.RobotStateResponse;
import com.robofleet.fleetstateservice.robot.domain.RobotState;
import com.robofleet.fleetstateservice.robot.infrastructure.messaging.RobotTelemetryEvent;
import com.robofleet.fleetstateservice.robot.infrastructure.persistence.RobotStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RobotStateServiceImpl implements RobotStateService {

    private final RobotStateRepository robotStateRepository;

    @Override
    public void upsertFromTelemetry(RobotTelemetryEvent event) {
        RobotState entity = RobotState.builder()
            .robotId(event.getRobotId())
            .x(event.getX())
            .y(event.getY())
            .battery(event.getBattery())
            .status(event.getStatus())
            .timestamp(event.getTimestamp())
            .build();

        robotStateRepository.save(entity);
    }

    @Override
    public List<RobotStateResponse> getAllRobots() {
        return robotStateRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public Optional<RobotStateResponse> getRobotById(String robotId) {
        return robotStateRepository.findById(robotId).map(this::toResponse);
    }

    private RobotStateResponse toResponse(RobotState entity) {
        return RobotStateResponse.builder()
            .robotId(entity.getRobotId())
            .x(entity.getX())
            .y(entity.getY())
            .battery(entity.getBattery())
            .status(entity.getStatus())
            .timestamp(entity.getTimestamp())
            .build();
    }
}
