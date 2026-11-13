package com.developersfab.Dhanushappbe.security;

import com.developersfab.Dhanushappbe.common.ErrorCode;
import com.developersfab.Dhanushappbe.common.ErrorResponseWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CsrfHeaderFilter csrfHeaderFilter;
    private final ErrorResponseWriter errorResponseWriter;

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) ->
                        errorResponseWriter.write(request, response, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED,
                                "Full authentication is required to access this resource.")
                ))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/signup",
                                "/auth/login",
                                "/auth/forgot-password",
                                "/auth/reset-password",
                                "/health",
                                "/uploads/files/**",
                                "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(csrfHeaderFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(
                frontendBaseUrl,
                "http://localhost:5173",
                "http://localhost:3000",
                "http://127.0.0.1:5173"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "X-Correlation-Id",
                "Accept",
                "Origin"
        ));
        config.setExposedHeaders(List.of("X-Correlation-Id", "Content-Disposition"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
