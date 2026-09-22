package com.example.jpaRepository;

import com.example.model.Emotion;
import com.example.model.enumerations.MoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmotionRepository extends JpaRepository<Emotion, Long> {
    List<Emotion> findByMoodCategory(MoodCategory category);
}