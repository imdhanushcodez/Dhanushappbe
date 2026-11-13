package com.developersfab.Dhanushappbe.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Business error carrying the HTTP status, the machine-readable code from the API spec
 * and optional field errors. Turned into an {@link ErrorResponse} by {@link GlobalExceptionHandler}.
 */
@Getter
public class AppException extends RuntimeException {
    private final HttpStatus status;
    private final ErrorCode errorCode;
    private final List<FieldErrorItem> fieldErrors;
    private final Long retryAfterSeconds;

    public AppException(HttpStatus status, ErrorCode errorCode, String message) {
        this(status, errorCode, message, null, null);
    }

    public AppException(HttpStatus status, ErrorCode errorCode, String message, List<FieldErrorItem> fieldErrors) {
        this(status, errorCode, message, fieldErrors, null);
    }

    public AppException(HttpStatus status, ErrorCode errorCode, String message,
                        List<FieldErrorItem> fieldErrors, Long retryAfterSeconds) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors != null ? fieldErrors : new ArrayList<>();
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public static AppException notFound() {
        return new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "The requested record was not found.");
    }

    public static AppException validation(String field, String code, String message) {
        return new AppException(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, "Validation failed.",
                List.of(new FieldErrorItem(field, code, message)));
    }

    public static AppException tooManyRequests(long retryAfterSeconds) {
        return new AppException(HttpStatus.TOO_MANY_REQUESTS, ErrorCode.TOO_MANY_REQUESTS,
                "Too many attempts. Please try again later.", null, retryAfterSeconds);
    }
}
