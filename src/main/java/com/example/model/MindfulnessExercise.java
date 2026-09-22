package com.example.model;

import com.example.model.enumerations.ExerciseType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class MindfulnessExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExerciseType type;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    private Integer timerDurationSeconds;

    private String animationIdentifier; // For front-end reference

    // TODO give option none
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sound_id")
    private CalmingSound sound;
}
