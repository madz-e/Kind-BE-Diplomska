package com.example.service;

import com.example.model.Emotion;
import com.example.model.enumerations.MoodCategory;

import java.util.List;
import java.util.Optional;

public interface EmotionService {
    List<Emotion> getAllEmotions();

    Optional<Emotion> findById(Long id);

    List<Emotion> findByMoodCategory(MoodCategory category);

    Emotion createEmotion(Emotion emotion);

    Emotion updateEmotion(Long id, Emotion emotion);

    void deleteEmotion(Long id);
}