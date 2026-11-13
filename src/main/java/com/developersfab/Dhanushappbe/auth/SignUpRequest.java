package com.developersfab.Dhanushappbe.auth;

import jakarta.validation.constraints.Email;
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
public class SignUpRequest {

    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is invalid.")
    @Size(max = 255, message = "Email cannot exceed 255 characters.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "Password must contain at least one letter and one number.")
    private String password;

    @NotBlank(message = "Mobile number is required.")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits.")
    private String mobile;
}

