package com.developersfab.Dhanushappbe.common;

import java.util.List;

public record ImportPreviewResponse(
        int totalRows,
        int validRows,
        int duplicateRows,
        int invalidRows,
        List<ImportRowError> errors
) {
}

