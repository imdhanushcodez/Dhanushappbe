package com.developersfab.Dhanushappbe.vault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaultEntrySummaryDto {
    private Long id;
    private String source;
    private String loginId;
    private Instant createdAt;
    private Instant updatedAt;
}

