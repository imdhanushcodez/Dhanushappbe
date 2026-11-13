package com.developersfab.Dhanushappbe.plan;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddFoodToDayRequest(
        @NotNull(message = "Day is required.")
        @Min(value = 1, message = "Day must be between 1 and 7.")
        @Max(value = 7, message = "Day must be between 1 and 7.")
        Integer day,

        @NotNull(message = "Food id is required.")
        @Min(value = 1, message = "Food id must be at least 1.")
        Long foodId
) {
}

