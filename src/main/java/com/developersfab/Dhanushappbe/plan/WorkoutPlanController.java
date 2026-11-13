package com.developersfab.Dhanushappbe.plan;

import com.developersfab.Dhanushappbe.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/plans/workouts")
@RequiredArgsConstructor
public class WorkoutPlanController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<WorkoutPlanWeekResponse> getWorkoutPlan() {
        Long userId = SecurityUtils.getCurrentUserId();
        WorkoutPlanWeekResponse response = planService.getWorkoutWeekPlan(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<WorkoutPlanEntryDto> addWorkoutToDay(@Valid @RequestBody AddWorkoutToDayRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        WorkoutPlanEntryDto created = planService.addWorkoutToDay(userId, request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> removeWorkoutFromDay(@PathVariable Long entryId) {
        Long userId = SecurityUtils.getCurrentUserId();
        planService.removeWorkoutFromDay(userId, entryId);
        return ResponseEntity.noContent().build();
    }
}

