package com.developersfab.Dhanushappbe.vault;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VaultEntryRequest(
        @NotBlank(message = "Source is required.")
        @Size(min = 1, max = 150, message = "Source must be between 1 and 150 characters.")
        String source,

        @NotBlank(message = "Id is required.")
        @Size(min = 1, max = 255, message = "Id must be between 1 and 255 characters.")
        String loginId,

        @NotBlank(message = "Password is required.")
        @Size(min = 1, max = 256, message = "Password must be between 1 and 256 characters.")
        String password
) {
}

