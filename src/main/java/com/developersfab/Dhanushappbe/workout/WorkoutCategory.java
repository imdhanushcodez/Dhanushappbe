package com.developersfab.Dhanushappbe.workout;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workout_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;
}

