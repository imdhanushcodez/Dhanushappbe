package com.developersfab.Dhanushappbe.workout;

import com.developersfab.Dhanushappbe.common.CategoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/workout-categories")
@RequiredArgsConstructor
public class WorkoutCategoryController {

    private final WorkoutService workoutService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> listWorkoutCategories() {
        List<CategoryDto> categories = workoutService.listCategories();
        return ResponseEntity.ok(categories);
    }
}

