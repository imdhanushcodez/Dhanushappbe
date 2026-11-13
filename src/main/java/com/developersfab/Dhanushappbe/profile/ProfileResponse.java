package com.developersfab.Dhanushappbe.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private String email;
    private String mobile;
    private String name;
    private String sex;
    private LocalDate dateOfBirth;
    private String profilePhotoUrl;
}

