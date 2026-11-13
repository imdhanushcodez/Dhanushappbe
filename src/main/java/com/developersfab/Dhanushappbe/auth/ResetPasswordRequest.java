package com.developersfab.Dhanushappbe.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Token is required.")
    private String token;

    @NotBlank(message = "New password is required.")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "Password must contain at least one letter and one number.")
    private String newPassword;
}

