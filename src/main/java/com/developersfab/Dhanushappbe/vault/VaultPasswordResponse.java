package com.developersfab.Dhanushappbe.vault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaultPasswordResponse {
    private String password;
}

