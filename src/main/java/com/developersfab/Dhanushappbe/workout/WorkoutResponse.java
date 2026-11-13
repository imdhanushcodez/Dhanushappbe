package com.developersfab.Dhanushappbe.workout;

import com.developersfab.Dhanushappbe.common.CategoryDto;

import java.math.BigDecimal;
import java.time.Instant;

public record WorkoutResponse(
        Long id,
        CategoryDto category,
        String name,
        Integer reps,
        BigDecimal weightKg,
        String imageUrl,
        Instant createdAt,
        Instant updatedAt
) {
}

