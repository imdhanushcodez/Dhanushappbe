package com.developersfab.Dhanushappbe.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Writes an {@link ErrorResponse} directly to the servlet response. Used by filters and
 * security handlers that run before Spring MVC (so @RestControllerAdvice cannot help).
 */
@Component
@RequiredArgsConstructor
public class ErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public void write(HttpServletRequest request, HttpServletResponse response,
                      HttpStatus status, ErrorCode code, String detail) throws IOException {
        Object cid = request.getAttribute(CorrelationIdFilter.CORRELATION_ID_KEY);
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), code, detail,
                request.getRequestURI(), cid != null ? cid.toString() : "", null);

        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}

