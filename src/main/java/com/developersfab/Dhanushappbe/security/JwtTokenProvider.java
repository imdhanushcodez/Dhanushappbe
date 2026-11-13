package com.developersfab.Dhanushappbe.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirySeconds;

    public JwtTokenProvider(
            @Value("${app.jwt.secret:dhanush-personal-app-jwt-secret-key-with-sufficient-entropy-32-chars!}") String secret,
            @Value("${app.jwt.expiry-seconds:7200}") long expirySeconds) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirySeconds = expirySeconds;
    }

    public String generateToken(Long userId) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirySeconds);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public Claims parseAndValidate(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public Long getUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public Instant getIssuedAt(Claims claims) {
        return claims.getIssuedAt().toInstant();
    }

    public long getExpirySeconds() {
        return expirySeconds;
    }
}

