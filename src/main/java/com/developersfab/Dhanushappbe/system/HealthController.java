package com.developersfab.Dhanushappbe.system;

import com.developersfab.Dhanushappbe.common.AppException;
import com.developersfab.Dhanushappbe.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return ResponseEntity.ok(new HealthResponse("UP"));
        } catch (Exception e) {
            log.error("Database health check failed", e);
            throw new AppException(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.SERVICE_UNAVAILABLE, "Database is unreachable.");
        }
    }
}

