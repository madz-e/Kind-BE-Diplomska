package com.example.service.impl;

import com.example.jpaRepository.AffirmationRepository;
import com.example.model.Affirmation;
import com.example.model.exceptions.AffirmationNotFoundException;
import com.example.model.exceptions.EmptyAffirmationTextException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class AffirmationService {
    // Pre-set affirmations as backup
    private static final List<String> DEFAULT_AFFIRMATIONS = Arrays.asList(
            "I am worthy of love and respect.",
            "I choose to see the good in every situation.",
            "My potential is limitless.",
            "I am capable of achieving my goals.",
            "I radiate positive energy.",
            "I am grateful for my strength and resilience.",
            "Every day, I grow stronger and wiser.",
            "I trust the journey of my life.",
            "I am at peace with who I am.",
            "I attract abundance and prosperity.",
            "My mind is calm and focused.",
            "I deserve happiness and fulfillment.",
            "I am confident in my abilities.",
            "I let go of what no longer serves me.",
            "I am creating a life I love."
    );

    private final AffirmationRepository affirmationRepository;

    @PostConstruct
    public void init() {
        if (affirmationRepository.count() == 0) {
            initializeDefaultAffirmations();
        }
    }

    // 1. Get random affirmation (for daily affirmation)
    public Affirmation getRandomAffirmation() {
// Try to get from database first
        Optional<Affirmation> randomAffirmation = affirmationRepository.findRandom();

        if (randomAffirmation.isPresent()) {
            return randomAffirmation.get();
        }

        // If database is empty, check if we should initialize it
        if (affirmationRepository.count() == 0) {
            initializeDefaultAffirmations();
        }
        return affirmationRepository.findRandom()
                .orElseThrow(() -> new RuntimeException("No affirmations found after initialization"));
    }

    // 2. Get all affirmations
    public List<Affirmation> getAllAffirmations() {
        List<Affirmation> affirmations = affirmationRepository.findAll();

        // If empty, initialize with defaults
        if (affirmations.isEmpty()) {
            initializeDefaultAffirmations();
            affirmations = affirmationRepository.findAll();
        }

        return affirmations;
    }

    @Transactional
    public void initializeDefaultAffirmations() {
        if (affirmationRepository.count() == 0) {
            for (String text : DEFAULT_AFFIRMATIONS) {
                Affirmation affirmation = new Affirmation();
                affirmation.setAffirmationText(text);
                affirmationRepository.save(affirmation);
            }
        }
    }

    /**
     * Get today's affirmation (same for entire day)
     * Uses day of year to pick consistent affirmation
     */
    public Affirmation getTodaysAffirmation() {
        List<Affirmation> allAffirmations = affirmationRepository.findAll();

        // If database is empty, try to initialize it
        if (allAffirmations.isEmpty()) {
            initializeDefaultAffirmations();
            allAffirmations = affirmationRepository.findAll();
        }

        // Use day of year to pick consistent affirmation per day
        int dayOfYear = java.time.LocalDate.now().getDayOfYear();
        int index = dayOfYear % allAffirmations.size();

        return allAffirmations.get(index);
    }

    // 3. Get affirmation by ID
    public Affirmation getAffirmationById(Long id) {
        return affirmationRepository.findById(id)
                .orElseThrow(() -> new AffirmationNotFoundException(id));
    }

    // 4. Save new affirmation
    public Affirmation createAffirmation(Affirmation affirmation) {
        if (affirmation.getAffirmationText() == null || affirmation.getAffirmationText().trim().isEmpty()) {
            throw new EmptyAffirmationTextException(affirmation.getAffirmationText());
        }
        return affirmationRepository.save(affirmation);
    }

    // 5. Update affirmation
    public Affirmation updateAffirmation(Long id, String newText) {
        if (newText == null || newText.trim().isEmpty()) {
            throw new EmptyAffirmationTextException(newText);
        }
        Affirmation affirmation = getAffirmationById(id);
        affirmation.setAffirmationText(newText);
        return affirmationRepository.save(affirmation);
    }

    // 6. Delete affirmation
    @Transactional
    public void deleteAffirmation(Long id) {
        // Check if affirmation exists
        Affirmation affirmation = getAffirmationById(id);
        affirmationRepository.deleteById(id);
    }

}