package com.developersfab.Dhanushappbe.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "Mobile number is required.")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits.")
    private String mobile;

    @Size(max = 100, message = "Name cannot exceed 100 characters.")
    private String name;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER|PREFER_NOT_TO_SAY)?$", message = "Sex must be MALE, FEMALE, OTHER, or PREFER_NOT_TO_SAY.")
    private String sex;

    @PastOrPresent(message = "Date of birth cannot be in the future.")
    private LocalDate dateOfBirth;

    @Size(max = 500, message = "Photo URL cannot exceed 500 characters.")
    private String profilePhotoUrl;
}

