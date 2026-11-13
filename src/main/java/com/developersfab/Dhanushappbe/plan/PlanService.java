package com.developersfab.Dhanushappbe.plan;

import com.developersfab.Dhanushappbe.auth.User;
import com.developersfab.Dhanushappbe.auth.UserRepository;
import com.developersfab.Dhanushappbe.common.AppException;
import com.developersfab.Dhanushappbe.common.ErrorCode;
import com.developersfab.Dhanushappbe.food.Food;
import com.developersfab.Dhanushappbe.food.FoodRepository;
import com.developersfab.Dhanushappbe.workout.Workout;
import com.developersfab.Dhanushappbe.workout.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final WorkoutPlanEntryRepository workoutPlanEntryRepository;
    private final FoodPlanEntryRepository foodPlanEntryRepository;
    private final WorkoutRepository workoutRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    public WorkoutPlanWeekResponse getWorkoutWeekPlan(Long userId) {
        List<WorkoutPlanEntry> allEntries = workoutPlanEntryRepository.findByUserIdOrderByDayAndPosition(userId);
        Map<Integer, List<WorkoutPlanEntry>> entriesByDay = allEntries.stream()
                .collect(Collectors.groupingBy(WorkoutPlanEntry::getDayOfWeek));

        List<WorkoutPlanDayDto> days = new ArrayList<>(7);
        for (int day = 1; day <= 7; day++) {
            List<WorkoutPlanEntry> dayEntries = entriesByDay.getOrDefault(day, Collections.emptyList());
            days.add(buildWorkoutPlanDay(day, dayEntries));
        }

        return new WorkoutPlanWeekResponse(days);
    }

    public WorkoutPlanDayDto getWorkoutDay(Long userId, int day) {
        validateDay(day);
        List<WorkoutPlanEntry> entries = workoutPlanEntryRepository.findByUserIdAndDayOfWeekOrderByPosition(userId, day);
        return buildWorkoutPlanDay(day, entries);
    }

    @Transactional
    public WorkoutPlanEntryDto addWorkoutToDay(Long userId, AddWorkoutToDayRequest request) {
        validateDay(request.day());
        User user = getUser(userId);

        Workout workout = workoutRepository.findById(request.workoutId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.WORKOUT_NOT_FOUND,
                        "Workout " + request.workoutId() + " does not exist."));

        int nextPos = workoutPlanEntryRepository.findMaxPositionByUserIdAndDayOfWeek(userId, request.day()) + 1;

        WorkoutPlanEntry entry = WorkoutPlanEntry.builder()
                .user(user)
                .dayOfWeek(request.day())
                .workout(workout)
                .position(nextPos)
                .build();

        WorkoutPlanEntry saved = workoutPlanEntryRepository.save(entry);
        return toWorkoutEntryDto(saved);
    }

    @Transactional
    public void removeWorkoutFromDay(Long userId, Long entryId) {
        WorkoutPlanEntry entry = workoutPlanEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Workout plan entry not found."));
        workoutPlanEntryRepository.delete(entry);
    }

    public FoodPlanWeekResponse getFoodWeekPlan(Long userId) {
        List<FoodPlanEntry> allEntries = foodPlanEntryRepository.findByUserIdOrderByDayAndPosition(userId);
        Map<Integer, List<FoodPlanEntry>> entriesByDay = allEntries.stream()
                .collect(Collectors.groupingBy(FoodPlanEntry::getDayOfWeek));

        List<FoodPlanDayDto> days = new ArrayList<>(7);
        for (int day = 1; day <= 7; day++) {
            List<FoodPlanEntry> dayEntries = entriesByDay.getOrDefault(day, Collections.emptyList());
            days.add(buildFoodPlanDay(day, dayEntries));
        }

        return new FoodPlanWeekResponse(days);
    }

    public FoodPlanDayDto getFoodDay(Long userId, int day) {
        validateDay(day);
        List<FoodPlanEntry> entries = foodPlanEntryRepository.findByUserIdAndDayOfWeekOrderByPosition(userId, day);
        return buildFoodPlanDay(day, entries);
    }

    @Transactional
    public FoodPlanEntryDto addFoodToDay(Long userId, AddFoodToDayRequest request) {
        validateDay(request.day());
        User user = getUser(userId);

        Food food = foodRepository.findById(request.foodId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.FOOD_NOT_FOUND,
                        "Food " + request.foodId() + " does not exist."));

        int nextPos = foodPlanEntryRepository.findMaxPositionByUserIdAndDayOfWeek(userId, request.day()) + 1;

        FoodPlanEntry entry = FoodPlanEntry.builder()
                .user(user)
                .dayOfWeek(request.day())
                .food(food)
                .position(nextPos)
                .build();

        FoodPlanEntry saved = foodPlanEntryRepository.save(entry);
        return toFoodEntryDto(saved);
    }

    @Transactional
    public void removeFoodFromDay(Long userId, Long entryId) {
        FoodPlanEntry entry = foodPlanEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Food plan entry not found."));
        foodPlanEntryRepository.delete(entry);
    }

    public DashboardResponse getDashboard(Long userId, int day) {
        validateDay(day);
        WorkoutPlanDayDto workoutDay = getWorkoutDay(userId, day);
        FoodPlanDayDto foodDay = getFoodDay(userId, day);
        return new DashboardResponse(day, workoutDay, foodDay);
    }

    private WorkoutPlanDayDto buildWorkoutPlanDay(int day, List<WorkoutPlanEntry> entries) {
        List<String> categories = entries.stream()
                .map(e -> e.getWorkout().getCategory().getName())
                .distinct()
                .toList();

        List<WorkoutPlanEntryDto> dtos = entries.stream()
                .map(this::toWorkoutEntryDto)
                .toList();

        return new WorkoutPlanDayDto(day, categories, dtos);
    }

    private FoodPlanDayDto buildFoodPlanDay(int day, List<FoodPlanEntry> entries) {
        BigDecimal totalCalories = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;

        for (FoodPlanEntry e : entries) {
            if (e.getFood().getCalories() != null) {
                totalCalories = totalCalories.add(e.getFood().getCalories());
            }
            if (e.getFood().getProteinG() != null) {
                totalProtein = totalProtein.add(e.getFood().getProteinG());
            }
        }

        totalCalories = totalCalories.setScale(2, RoundingMode.HALF_UP);
        totalProtein = totalProtein.setScale(2, RoundingMode.HALF_UP);

        List<FoodPlanEntryDto> dtos = entries.stream()
                .map(this::toFoodEntryDto)
                .toList();

        return new FoodPlanDayDto(day, totalCalories, totalProtein, dtos);
    }

    private WorkoutPlanEntryDto toWorkoutEntryDto(WorkoutPlanEntry e) {
        Workout w = e.getWorkout();
        return new WorkoutPlanEntryDto(
                e.getId(),
                e.getDayOfWeek(),
                e.getPosition(),
                w.getId(),
                w.getName(),
                w.getCategory().getName(),
                w.getReps(),
                w.getWeightKg(),
                w.getImageUrl()
        );
    }

    private FoodPlanEntryDto toFoodEntryDto(FoodPlanEntry e) {
        Food f = e.getFood();
        return new FoodPlanEntryDto(
                e.getId(),
                e.getDayOfWeek(),
                e.getPosition(),
                f.getId(),
                f.getDishName(),
                f.getCategory().getName(),
                f.getWeightG(),
                f.getCalories(),
                f.getProteinG(),
                f.getFiberG(),
                f.getImageUrl()
        );
    }

    private void validateDay(int day) {
        if (day < 1 || day > 7) {
            throw new AppException(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, "Day must be between 1 and 7.");
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "User not found."));
    }
}

