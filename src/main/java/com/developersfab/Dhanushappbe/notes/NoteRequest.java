package com.developersfab.Dhanushappbe.notes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteRequest(
        @NotBlank(message = "Topic is required.")
        @Size(min = 1, max = 200, message = "Topic must be between 1 and 200 characters.")
        String topic,

        @NotBlank(message = "Notes text is required.")
        @Size(min = 1, max = 10000, message = "Notes must be between 1 and 10000 characters.")
        String notes
) {
}

