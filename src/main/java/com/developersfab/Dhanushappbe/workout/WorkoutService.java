package com.developersfab.Dhanushappbe.workout;

import com.developersfab.Dhanushappbe.common.AppException;
import com.developersfab.Dhanushappbe.common.CategoryDto;
import com.developersfab.Dhanushappbe.common.ErrorCode;
import com.developersfab.Dhanushappbe.plan.WorkoutPlanEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutCategoryRepository categoryRepository;
    private final WorkoutRepository workoutRepository;
    private final WorkoutPlanEntryRepository planEntryRepository;

    public List<CategoryDto> listCategories() {
        return categoryRepository.findAllByOrderByIdAsc().stream()
                .map(c -> new CategoryDto(c.getId(), c.getName()))
                .toList();
    }

    public List<WorkoutResponse> listWorkouts(Integer categoryId) {
        return workoutRepository.findAllByCategoryIdOrder(categoryId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public WorkoutResponse createWorkout(WorkoutRequest request) {
        WorkoutCategory category = getCategory(request.categoryId());

        Workout workout = Workout.builder()
                .category(category)
                .name(request.name().trim())
                .reps(request.reps())
                .weightKg(request.weightKg())
                .imageUrl(request.imageUrl() != null && !request.imageUrl().isBlank() ? request.imageUrl().trim() : null)
                .build();

        Workout saved = workoutRepository.save(workout);
        return toResponse(saved);
    }

    public WorkoutResponse getWorkout(Long id) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Workout not found."));
        return toResponse(workout);
    }

    @Transactional
    public WorkoutResponse updateWorkout(Long id, WorkoutRequest request) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Workout not found."));

        WorkoutCategory category = getCategory(request.categoryId());

        workout.setCategory(category);
        workout.setName(request.name().trim());
        workout.setReps(request.reps());
        workout.setWeightKg(request.weightKg());
        workout.setImageUrl(request.imageUrl() != null && !request.imageUrl().isBlank() ? request.imageUrl().trim() : null);

        Workout updated = workoutRepository.save(workout);
        return toResponse(updated);
    }

    @Transactional
    public void deleteWorkout(Long id) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Workout not found."));

        if (planEntryRepository.existsByWorkoutId(id)) {
            throw new AppException(HttpStatus.CONFLICT, ErrorCode.WORKOUT_IN_USE, "This workout is used in a plan and cannot be deleted.");
        }

        workoutRepository.delete(workout);
    }

    private WorkoutCategory getCategory(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Category not found."));
    }

    public WorkoutResponse toResponse(Workout w) {
        return new WorkoutResponse(
                w.getId(),
                new CategoryDto(w.getCategory().getId(), w.getCategory().getName()),
                w.getName(),
                w.getReps(),
                w.getWeightKg(),
                w.getImageUrl(),
                w.getCreatedAt(),
                w.getUpdatedAt()
        );
    }
}

