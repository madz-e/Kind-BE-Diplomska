package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalmingSound {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String audioUrl;

    @Column(nullable = false)
    private boolean loopEnabled;

    @JsonIgnore
    @OneToMany(mappedBy = "sound", fetch = FetchType.LAZY)
    private Set<MindfulnessExercise> exercises;
}
