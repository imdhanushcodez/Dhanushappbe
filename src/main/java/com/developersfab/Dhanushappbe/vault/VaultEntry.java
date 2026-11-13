package com.developersfab.Dhanushappbe.vault;

import com.developersfab.Dhanushappbe.auth.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "vault_entries",
        uniqueConstraints = @UniqueConstraint(name = "uk_vault_user_source_login", columnNames = {"user_id", "source", "login_id"}),
        indexes = @Index(name = "idx_vault_user_source", columnList = "user_id, source")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaultEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String source;

    @Column(name = "login_id", nullable = false, length = 255)
    private String loginId;

    @Column(name = "password_encrypted", nullable = false, columnDefinition = "TEXT")
    private String passwordEncrypted;

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
        if (source != null) source = source.trim();
        if (loginId != null) loginId = loginId.trim();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
        if (source != null) source = source.trim();
        if (loginId != null) loginId = loginId.trim();
    }
}

