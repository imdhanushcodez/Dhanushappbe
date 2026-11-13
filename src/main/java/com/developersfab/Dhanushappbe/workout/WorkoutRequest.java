package com.developersfab.Dhanushappbe.workout;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record WorkoutRequest(
        @NotNull(message = "Category id is required.")
        @Min(value = 1, message = "Category id must be at least 1.")
        Integer categoryId,

        @NotBlank(message = "Name is required.")
        @Size(min = 1, max = 150, message = "Name must be between 1 and 150 characters.")
        String name,

        @NotNull(message = "Reps is required.")
        @Min(value = 1, message = "Reps must be at least 1.")
        Integer reps,

        @DecimalMin(value = "0.01", message = "Weight must be greater than 0.")
        BigDecimal weightKg,

        @Size(max = 500, message = "Image URL must not exceed 500 characters.")
        String imageUrl
) {
}

