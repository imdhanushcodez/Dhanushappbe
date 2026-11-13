package com.developersfab.Dhanushappbe.auth;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_SECONDS = 15 * 60; // 15 minutes

    private static class Attempt {
        int count;
        Instant windowStart;

        Attempt(Instant windowStart) {
            this.count = 1;
            this.windowStart = windowStart;
        }
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isAllowed(String key) {
        Instant now = Instant.now();
        attempts.compute(key, (k, existing) -> {
            if (existing == null || now.isAfter(existing.windowStart.plusSeconds(WINDOW_SECONDS))) {
                return new Attempt(now);
            }
            existing.count++;
            return existing;
        });

        Attempt current = attempts.get(key);
        return current != null && current.count <= MAX_ATTEMPTS;
    }

    public void reset(String key) {
        attempts.remove(key);
    }
}

