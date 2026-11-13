package com.developersfab.Dhanushappbe.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String email;
    private String mobile;
    private String name;
    private String profilePhotoUrl;
    private String token;
}

