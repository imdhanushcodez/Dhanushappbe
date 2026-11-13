package com.developersfab.Dhanushappbe.common;

import com.developersfab.Dhanushappbe.food.FoodCategory;
import com.developersfab.Dhanushappbe.food.FoodCategoryRepository;
import com.developersfab.Dhanushappbe.workout.WorkoutCategory;
import com.developersfab.Dhanushappbe.workout.WorkoutCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final WorkoutCategoryRepository workoutCategoryRepository;
    private final FoodCategoryRepository foodCategoryRepository;

    @Override
    public void run(String... args) {
        initWorkoutCategories();
        initFoodCategories();
    }

    private void initWorkoutCategories() {
        if (workoutCategoryRepository.count() == 0) {
            log.info("Seeding default workout categories...");
            List<WorkoutCategory> categories = List.of(
                    WorkoutCategory.builder().id(1).name("Chest").build(),
                    WorkoutCategory.builder().id(2).name("Back").build(),
                    WorkoutCategory.builder().id(3).name("Shoulders").build(),
                    WorkoutCategory.builder().id(4).name("Biceps").build(),
                    WorkoutCategory.builder().id(5).name("Triceps").build(),
                    WorkoutCategory.builder().id(6).name("Legs").build(),
                    WorkoutCategory.builder().id(7).name("ABS").build(),
                    WorkoutCategory.builder().id(8).name("Cardio").build(),
                    WorkoutCategory.builder().id(9).name("Other").build()
            );
            workoutCategoryRepository.saveAll(categories);
        }
    }

    private void initFoodCategories() {
        if (foodCategoryRepository.count() == 0) {
            log.info("Seeding default food categories...");
            List<FoodCategory> categories = List.of(
                    FoodCategory.builder().id(1).name("Breakfast").build(),
                    FoodCategory.builder().id(2).name("Lunch").build(),
                    FoodCategory.builder().id(3).name("Dinner").build(),
                    FoodCategory.builder().id(4).name("Snacks").build(),
                    FoodCategory.builder().id(5).name("Fruits").build(),
                    FoodCategory.builder().id(6).name("Drinks").build(),
                    FoodCategory.builder().id(7).name("Other").build()
            );
            foodCategoryRepository.saveAll(categories);
        }
    }
}

