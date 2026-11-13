package com.developersfab.Dhanushappbe.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** The single error body used by every error response (see ErrorResponse in the API spec). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String type;
    private String title;
    private int status;
    private String code;
    private String detail;
    private String instance;
    private String correlationId;
    @Builder.Default
    private List<FieldErrorItem> errors = new ArrayList<>();

    public static ErrorResponse of(int status, String title, ErrorCode code, String detail,
                                   String instance, String correlationId, List<FieldErrorItem> errors) {
        return ErrorResponse.builder()
                .type("https://personalapp.example/errors/" + code.name().toLowerCase().replace('_', '-'))
                .title(title)
                .status(status)
                .code(code.name())
                .detail(detail)
                .instance(instance)
                .correlationId(correlationId == null ? "" : correlationId)
                .errors(errors == null ? new ArrayList<>() : errors)
                .build();
    }
}
