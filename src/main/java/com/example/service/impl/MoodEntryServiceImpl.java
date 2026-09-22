package com.example.service.impl;

import com.example.jpaRepository.EmotionRepository;
import com.example.jpaRepository.MoodEntryRepository;
import com.example.jpaRepository.MoodFactorRepository;
import com.example.jpaRepository.UserRepository;
import com.example.model.Emotion;
import com.example.model.MoodEntry;
import com.example.model.MoodFactor;
import com.example.model.User;
import com.example.model.enumerations.MoodCategory;
import com.example.service.MoodEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MoodEntryServiceImpl implements MoodEntryService {

    private final MoodEntryRepository moodEntryRepository;
    private final UserRepository userRepository;
    private final EmotionRepository emotionRepository;
    private final MoodFactorRepository moodFactorRepository;

    @Override
    public MoodEntry createMoodEntry(MoodEntry moodEntry) {
        // Validate user exists
        User user = userRepository.findById(moodEntry.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        moodEntry.setUser(user);

        // Validate and set emotions
        if (moodEntry.getSelectedEmotions() != null && !moodEntry.getSelectedEmotions().isEmpty()) {
            Set<Emotion> emotions = moodEntry.getSelectedEmotions().stream()
                    .map(emotion -> emotionRepository.findById(emotion.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Emotion not found with id: " + emotion.getId())))
                    .collect(Collectors.toSet());
            moodEntry.setSelectedEmotions(emotions);
        }

        // Validate and set mood factors
        if (moodEntry.getSelectedFactors() != null && !moodEntry.getSelectedFactors().isEmpty()) {
            Set<MoodFactor> factors = moodEntry.getSelectedFactors().stream()
                    .map(factor -> moodFactorRepository.findById(factor.getId())
                            .orElseThrow(() -> new IllegalArgumentException("MoodFactor not found with id: " + factor.getId())))
                    .collect(Collectors.toSet());
            moodEntry.setSelectedFactors(factors);
        }

        // Mood category is automatically set by the setMoodValue method in MoodEntry
        return moodEntryRepository.save(moodEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MoodEntry> findById(Long id) {
        return moodEntryRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoodEntry> findByUserId(Long userId) {
        return moodEntryRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MoodEntry> findByUserIdAndDate(Long userId, LocalDate date) {
        return moodEntryRepository.findByUserIdAndDate(userId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoodEntry> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return moodEntryRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoodEntry> findByUserIdAndMoodCategory(Long userId, MoodCategory category) {
        return moodEntryRepository.findByUserIdAndMoodCategory(userId, category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoodEntry> findByUserIdAndYearMonth(Long userId, int year, int month) {
        return moodEntryRepository.findByUserIdAndYearMonth(userId, year, month);
    }

    @Override
    public MoodEntry updateMoodEntry(Long id, MoodEntry moodEntry) {
        MoodEntry existingEntry = moodEntryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MoodEntry not found with id: " + id));

        existingEntry.setMoodValue(moodEntry.getMoodValue()); // This auto-sets category
        existingEntry.setNote(moodEntry.getNote());

        // Update emotions
        if (moodEntry.getSelectedEmotions() != null) {
            Set<Emotion> emotions = moodEntry.getSelectedEmotions().stream()
                    .map(emotion -> emotionRepository.findById(emotion.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Emotion not found with id: " + emotion.getId())))
                    .collect(Collectors.toSet());
            existingEntry.setSelectedEmotions(emotions);
        }

        // Update factors
        if (moodEntry.getSelectedFactors() != null) {
            Set<MoodFactor> factors = moodEntry.getSelectedFactors().stream()
                    .map(factor -> moodFactorRepository.findById(factor.getId())
                            .orElseThrow(() -> new IllegalArgumentException("MoodFactor not found with id: " + factor.getId())))
                    .collect(Collectors.toSet());
            existingEntry.setSelectedFactors(factors);
        }

        return moodEntryRepository.save(existingEntry);
    }

    @Override
    public void deleteMoodEntry(Long id) {
        if (!moodEntryRepository.existsById(id)) {
            throw new IllegalArgumentException("MoodEntry not found with id: " + id);
        }
        moodEntryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateAverageMood(Long userId, LocalDate startDate, LocalDate endDate) {
        return moodEntryRepository.calculateAverageMood(userId, startDate, endDate);
    }
}