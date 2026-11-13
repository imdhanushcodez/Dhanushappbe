package com.developersfab.Dhanushappbe.notes;

import java.time.Instant;

public record NoteSummary(
        Long id,
        String topic,
        String preview,
        Instant createdAt,
        Instant updatedAt
) {
}

