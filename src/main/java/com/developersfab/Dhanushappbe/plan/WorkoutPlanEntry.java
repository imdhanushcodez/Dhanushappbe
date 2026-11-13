package com.developersfab.Dhanushappbe.plan;

import com.developersfab.Dhanushappbe.auth.User;
import com.developersfab.Dhanushappbe.workout.Workout;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "workout_plan_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutPlanEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

