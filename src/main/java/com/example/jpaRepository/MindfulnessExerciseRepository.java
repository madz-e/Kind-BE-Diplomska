package com.example.jpaRepository;

import com.example.model.MindfulnessExercise;
import com.example.model.enumerations.ExerciseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MindfulnessExerciseRepository extends JpaRepository<MindfulnessExercise, Long> {

    // Get exercises by type (BREATHING or MEDITATION)
    List<MindfulnessExercise> findByType(ExerciseType type);
}