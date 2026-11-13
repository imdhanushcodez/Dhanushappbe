package com.developersfab.Dhanushappbe.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, unique = true, length = 15)
    private String mobile;

    @Column(length = 100)
    private String name;

    @Column(length = 20)
    private String sex;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    @Column(name = "password_changed_at", nullable = false)
    @Builder.Default
    private Instant passwordChangedAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (passwordChangedAt == null) passwordChangedAt = now;
        if (email != null) email = email.toLowerCase().trim();
        if (mobile != null) mobile = mobile.trim();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
        if (email != null) email = email.toLowerCase().trim();
        if (mobile != null) mobile = mobile.trim();
    }
}

