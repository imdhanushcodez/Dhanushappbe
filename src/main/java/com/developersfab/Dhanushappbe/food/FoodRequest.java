package com.developersfab.Dhanushappbe.food;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FoodRequest(
        @NotNull(message = "Category id is required.")
        @Min(value = 1, message = "Category id must be at least 1.")
        Integer categoryId,

        @NotBlank(message = "Dish name is required.")
        @Size(min = 1, max = 150, message = "Dish name must be between 1 and 150 characters.")
        String dishName,

        @NotNull(message = "Weight is required.")
        @DecimalMin(value = "0.01", message = "Weight must be greater than 0.")
        BigDecimal weightG,

        @NotNull(message = "Calories is required.")
        @DecimalMin(value = "0.01", message = "Calories must be greater than 0.")
        BigDecimal calories,

        @NotNull(message = "Protein is required.")
        @DecimalMin(value = "0.00", message = "Protein cannot be negative.")
        BigDecimal proteinG,

        @NotNull(message = "Fiber is required.")
        @DecimalMin(value = "0.00", message = "Fiber cannot be negative.")
        BigDecimal fiberG,

        @Size(max = 500, message = "Image URL must not exceed 500 characters.")
        String imageUrl,

        @Size(max = 1000, message = "Others must not exceed 1000 characters.")
        String others
) {
}

