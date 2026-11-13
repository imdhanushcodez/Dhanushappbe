package com.developersfab.Dhanushappbe.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId(request);
        ErrorResponse body = ErrorResponse.builder()
                .type("https://personalapp.example/errors/" + ex.getErrorCode().name().toLowerCase().replace('_', '-'))
                .title(ex.getStatus().getReasonPhrase())
                .status(ex.getStatus().value())
                .code(ex.getErrorCode().name())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .correlationId(correlationId)
                .errors(ex.getFieldErrors())
                .build();

        return new ResponseEntity<>(body, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId(request);
        List<FieldErrorItem> errors = new ArrayList<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String code = mapValidationCode(fieldError.getCode());
            errors.add(FieldErrorItem.builder()
                    .field(fieldError.getField())
                    .code(code)
                    .message(fieldError.getDefaultMessage())
                    .build());
        }

        ErrorResponse body = ErrorResponse.builder()
                .type("https://personalapp.example/errors/validation-failed")
                .title("Bad Request")
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.VALIDATION_FAILED.name())
                .detail("One or more fields failed validation.")
                .instance(request.getRequestURI())
                .correlationId(correlationId)
                .errors(errors)
                .build();

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId(request);
        ErrorResponse body = ErrorResponse.builder()
                .type("https://personalapp.example/errors/file-too-large")
                .title("Payload Too Large")
                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .code(ErrorCode.FILE_TOO_LARGE.name())
                .detail("Uploaded file exceeds maximum limit of 2 MB.")
                .instance(request.getRequestURI())
                .correlationId(correlationId)
                .build();

        return new ResponseEntity<>(body, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        String correlationId = getCorrelationId(request);
        log.error("Unhandled internal server error on {} [correlationId={}]", request.getRequestURI(), correlationId, ex);
        ErrorResponse body = ErrorResponse.builder()
                .type("https://personalapp.example/errors/internal-error")
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code(ErrorCode.INTERNAL_ERROR.name())
                .detail("An unexpected error occurred.")
                .instance(request.getRequestURI())
                .correlationId(correlationId)
                .build();

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getCorrelationId(HttpServletRequest request) {
        Object cid = request.getAttribute(CorrelationIdFilter.CORRELATION_ID_KEY);
        return cid != null ? cid.toString() : "";
    }

    private String mapValidationCode(String springCode) {
        if (springCode == null) return "INVALID_FORMAT";
        return switch (springCode) {
            case "NotNull", "NotEmpty", "NotBlank" -> "REQUIRED";
            case "Size" -> "OUT_OF_RANGE";
            case "Min" -> "TOO_SHORT";
            case "Max" -> "TOO_LONG";
            case "Email", "Pattern" -> "INVALID_FORMAT";
            default -> "INVALID_FORMAT";
        };
    }
}

