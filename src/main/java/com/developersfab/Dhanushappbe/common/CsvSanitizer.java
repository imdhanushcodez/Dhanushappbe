package com.developersfab.Dhanushappbe.common;

public final class CsvSanitizer {

    private CsvSanitizer() {}

    /**
     * Neutralizes spreadsheet formula injection (=, +, -, @) on export
     */
    public static String sanitizeForExport(String value) {
        if (value == null) return "";
        if (value.startsWith("=") || value.startsWith("+") || value.startsWith("-") || value.startsWith("@")) {
            return "'" + value;
        }
        return value;
    }

    public static String sanitizeForCsv(String value) {
        return sanitizeForExport(value);
    }

    /**
     * Strips leading single quote introduced for formula neutralization on import
     */
    public static String cleanFromImport(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        if (trimmed.startsWith("'") && (trimmed.startsWith("'=") || trimmed.startsWith("'+") || trimmed.startsWith("'-") || trimmed.startsWith("'@"))) {
            return trimmed.substring(1);
        }
        return trimmed;
    }
}

