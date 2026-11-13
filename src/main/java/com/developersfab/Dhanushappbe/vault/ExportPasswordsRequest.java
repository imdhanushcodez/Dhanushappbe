package com.developersfab.Dhanushappbe.vault;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExportPasswordsRequest(
        @NotBlank(message = "Current password is required.")
        @Size(min = 1, max = 72, message = "Current password must be between 1 and 72 characters.")
        String currentPassword
) {
}

