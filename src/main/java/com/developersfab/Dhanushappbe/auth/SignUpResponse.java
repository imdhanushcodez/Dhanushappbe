package com.developersfab.Dhanushappbe.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResponse {
    private Long id;
    private String email;
    private String mobile;
}

