package com.developersfab.Dhanushappbe.food;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "food_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;
}

