package com.example.service.impl;

import com.example.jpaRepository.MoodFactorRepository;
import com.example.model.MoodFactor;
import com.example.service.MoodFactorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MoodFactorServiceImpl implements MoodFactorService {

    private final MoodFactorRepository moodFactorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MoodFactor> getAllMoodFactors() {
        return moodFactorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MoodFactor> findById(Long id) {
        return moodFactorRepository.findById(id);
    }

    @Override
    public MoodFactor createMoodFactor(MoodFactor moodFactor) {
        return moodFactorRepository.save(moodFactor);
    }

    @Override
    public MoodFactor updateMoodFactor(Long id, MoodFactor moodFactor) {
        MoodFactor existingFactor = moodFactorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MoodFactor not found with id: " + id));

        existingFactor.setName(moodFactor.getName());

        return moodFactorRepository.save(existingFactor);
    }

    @Override
    public void deleteMoodFactor(Long id) {
        if (!moodFactorRepository.existsById(id)) {
            throw new IllegalArgumentException("MoodFactor not found with id: " + id);
        }
        moodFactorRepository.deleteById(id);
    }
}