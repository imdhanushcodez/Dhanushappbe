package com.developersfab.Dhanushappbe.security;

import com.developersfab.Dhanushappbe.common.ErrorCode;
import com.developersfab.Dhanushappbe.common.ErrorResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CsrfHeaderFilter extends OncePerRequestFilter {

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");
    private final ErrorResponseWriter errorResponseWriter;

    @Value("${app.security.csrf-check-enabled:true}")
    private boolean csrfCheckEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (csrfCheckEnabled && MUTATING_METHODS.contains(request.getMethod())) {
            String requestedWith = request.getHeader("X-Requested-With");
            if (requestedWith == null || !"XMLHttpRequest".equalsIgnoreCase(requestedWith.trim())) {
                errorResponseWriter.write(request, response, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN,
                        "Missing required X-Requested-With: XMLHttpRequest header.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
