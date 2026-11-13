package com.developersfab.Dhanushappbe.workout;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    @GetMapping
    public ResponseEntity<List<WorkoutResponse>> listWorkouts(@RequestParam(required = false) Integer categoryId) {
        List<WorkoutResponse> workouts = workoutService.listWorkouts(categoryId);
        return ResponseEntity.ok(workouts);
    }

    @PostMapping
    public ResponseEntity<WorkoutResponse> createWorkout(@Valid @RequestBody WorkoutRequest request) {
        WorkoutResponse created = workoutService.createWorkout(request);
        URI location = URI.create("/api/workouts/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutResponse> getWorkout(@PathVariable Long id) {
        WorkoutResponse workout = workoutService.getWorkout(id);
        return ResponseEntity.ok(workout);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutResponse> updateWorkout(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutRequest request) {
        WorkoutResponse updated = workoutService.updateWorkout(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkout(@PathVariable Long id) {
        workoutService.deleteWorkout(id);
        return ResponseEntity.noContent().build();
    }
}

