package com.developersfab.Dhanushappbe.common;

import org.springframework.data.domain.Page;

import java.util.List;

/** Generic page body: {@code items, page, size, totalItems, totalPages} (VaultEntryPage / NotePage). */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}

