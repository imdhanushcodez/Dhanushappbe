package com.developersfab.Dhanushappbe.security;

import com.developersfab.Dhanushappbe.auth.User;
import com.developersfab.Dhanushappbe.auth.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Value("${app.jwt.cookie-name:access_token}")
    private String cookieName;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);
        if (token != null) {
            Claims claims = tokenProvider.parseAndValidate(token);
            if (claims != null) {
                Long userId = tokenProvider.getUserId(claims);
                Instant iat = tokenProvider.getIssuedAt(claims);

                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    // Reject token if issued before the last password change
                    if (user.getPasswordChangedAt() == null || !iat.isBefore(user.getPasswordChangedAt().minusSeconds(1))) {
                        UserPrincipal principal = new UserPrincipal(
                                user.getId(),
                                user.getEmail(),
                                user.getPasswordHash(),
                                user.getPasswordChangedAt()
                        );

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1. Try Cookie
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (cookieName.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                    return c.getValue();
                }
            }
        }

        // 2. Fallback to Authorization: Bearer <token> header for convenience (Postman/Curl)
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7).trim();
        }

        return null;
    }
}

