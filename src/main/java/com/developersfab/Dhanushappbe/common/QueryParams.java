package com.developersfab.Dhanushappbe.common;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Validation of list query parameters (q, sort, page, size, addedFrom, addedTo, day). */
public final class QueryParams {

    public static final int MAX_QUERY_LENGTH = 100;
    public static final int MAX_PAGE_SIZE = 100;

    private QueryParams() {}

    public record ListQuery(String q, String sort, int page, int size, Instant addedFrom, Instant addedTo) {}

    public static ListQuery validateList(String q, String sort, Set<String> allowedSorts,
                                         Integer page, Integer size, String addedFrom, String addedTo) {
        List<FieldErrorItem> errors = new ArrayList<>();

        String cleanQ = (q == null || q.isBlank()) ? null : q.trim();
        if (cleanQ != null && cleanQ.length() > MAX_QUERY_LENGTH) {
            errors.add(new FieldErrorItem("q", "TOO_LONG", "Search text can be at most 100 characters."));
        }

        String cleanSort = (sort == null || sort.isBlank()) ? "newest" : sort.trim();
        if (!allowedSorts.contains(cleanSort)) {
            errors.add(new FieldErrorItem("sort", "INVALID_FORMAT", "Sort must be one of " + allowedSorts + "."));
        }

        int p = page == null ? 0 : page;
        if (p < 0) {
            errors.add(new FieldErrorItem("page", "OUT_OF_RANGE", "Page must be 0 or more."));
        }
        int s = size == null ? 50 : size;
        if (s < 1 || s > MAX_PAGE_SIZE) {
            errors.add(new FieldErrorItem("size", "OUT_OF_RANGE", "Size must be between 1 and 100."));
        }

        Instant from = parseInstant(addedFrom, "addedFrom", false, errors);
        Instant to = parseInstant(addedTo, "addedTo", true, errors);
        if (from != null && to != null && from.isAfter(to)) {
            errors.add(new FieldErrorItem("addedFrom", "OUT_OF_RANGE", "addedFrom must be before addedTo."));
        }

        if (!errors.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, "Validation failed.", errors);
        }
        return new ListQuery(cleanQ, cleanSort, p, s, from, to);
    }

    public static short validateDay(Integer day, String field) {
        if (day == null) {
            throw AppException.validation(field, "REQUIRED", "Day is required (1 = Monday to 7 = Sunday).");
        }
        if (day < 1 || day > 7) {
            throw AppException.validation(field, "OUT_OF_RANGE", "Day must be between 1 (Monday) and 7 (Sunday).");
        }
        return day.shortValue();
    }

    /** Escapes LIKE wildcards so user text is matched literally (escape character is backslash). */
    public static String likePattern(String q) {
        String escaped = q.toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    /**
     * Accepts ISO 8601 date-time (2026-10-30T00:00:00Z / with offset). A plain date (2026-10-30) is
     * also accepted for convenience: start of day for "from", end of day for "to" (UTC).
     */
    private static Instant parseInstant(String value, String field, boolean endOfDay, List<FieldErrorItem> errors) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim();
        try {
            return OffsetDateTime.parse(v).toInstant();
        } catch (Exception ignored) {
            // try next format
        }
        try {
            return Instant.parse(v);
        } catch (Exception ignored) {
            // try next format
        }
        try {
            LocalDate d = LocalDate.parse(v);
            return endOfDay
                    ? d.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusNanos(1)
                    : d.atStartOfDay().toInstant(ZoneOffset.UTC);
        } catch (Exception e) {
            errors.add(new FieldErrorItem(field, "INVALID_FORMAT", field + " must be an ISO 8601 date-time."));
            return null;
        }
    }
}

