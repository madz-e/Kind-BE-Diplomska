package com.example.service.impl;

import com.example.jpaRepository.EmotionRepository;
import com.example.model.Emotion;
import com.example.model.enumerations.MoodCategory;
import com.example.service.EmotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmotionServiceImpl implements EmotionService {

    private final EmotionRepository emotionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Emotion> getAllEmotions() {
        return emotionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Emotion> findById(Long id) {
        return emotionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Emotion> findByMoodCategory(MoodCategory category) {
        return emotionRepository.findByMoodCategory(category);
    }

    @Override
    public Emotion createEmotion(Emotion emotion) {
        return emotionRepository.save(emotion);
    }

    @Override
    public Emotion updateEmotion(Long id, Emotion emotion) {
        Emotion existingEmotion = emotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Emotion not found with id: " + id));

        existingEmotion.setName(emotion.getName());
        existingEmotion.setMoodCategory(emotion.getMoodCategory());

        return emotionRepository.save(existingEmotion);
    }

    @Override
    public void deleteEmotion(Long id) {
        if (!emotionRepository.existsById(id)) {
            throw new IllegalArgumentException("Emotion not found with id: " + id);
        }
        emotionRepository.deleteById(id);
    }
}