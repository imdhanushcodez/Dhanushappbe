package com.developersfab.Dhanushappbe.notes;

import java.time.Instant;

public record NoteResponse(
        Long id,
        String topic,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}

