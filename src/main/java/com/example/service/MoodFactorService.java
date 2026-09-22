package com.example.service;

import com.example.model.MoodFactor;

import java.util.List;
import java.util.Optional;

public interface MoodFactorService {
    List<MoodFactor> getAllMoodFactors();

    Optional<MoodFactor> findById(Long id);

    MoodFactor createMoodFactor(MoodFactor moodFactor);

    MoodFactor updateMoodFactor(Long id, MoodFactor moodFactor);

    void deleteMoodFactor(Long id);
}