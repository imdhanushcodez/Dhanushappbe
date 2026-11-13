package com.developersfab.Dhanushappbe.food;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "foods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private FoodCategory category;

    @Column(name = "dish_name", nullable = false, length = 150)
    private String dishName;

    @Column(name = "weight_g", nullable = false, precision = 6, scale = 2)
    private BigDecimal weightG;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal calories;

    @Column(name = "protein_g", nullable = false, precision = 6, scale = 2)
    private BigDecimal proteinG;

    @Column(name = "fiber_g", nullable = false, precision = 6, scale = 2)
    private BigDecimal fiberG;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "others", columnDefinition = "TEXT")
    private String others;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}

