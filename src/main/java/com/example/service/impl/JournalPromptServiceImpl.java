package com.example.service.impl;

import com.example.jpaRepository.JournalPromptRepository;
import com.example.model.JournalPrompt;
import com.example.model.enumerations.JournalPromptType;
import com.example.service.JournalPromptService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class JournalPromptServiceImpl implements JournalPromptService {

    private final JournalPromptRepository journalPromptRepository;

    // Ensure the CBT thought-record prompt exists so entries can link to it.
    // Idempotent: only creates one if no ANT_EXERCISE prompt is present yet.
    @PostConstruct
    public void seedAntExercisePrompt() {
        if (journalPromptRepository.findByType(JournalPromptType.ANT_EXERCISE).isEmpty()) {
            JournalPrompt prompt = new JournalPrompt();
            prompt.setPromptText("Thought Record — challenge an unhelpful thought");
            prompt.setType(JournalPromptType.ANT_EXERCISE);
            journalPromptRepository.save(prompt);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<JournalPrompt> getAllPrompts() {
        return journalPromptRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JournalPrompt> findById(Long id) {
        return journalPromptRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JournalPrompt> findByType(JournalPromptType type) {
        return journalPromptRepository.findByType(type);
    }

    @Override
    public JournalPrompt createPrompt(JournalPrompt prompt) {
        return journalPromptRepository.save(prompt);
    }

    @Override
    public JournalPrompt updatePrompt(Long id, JournalPrompt prompt) {
        JournalPrompt existingPrompt = journalPromptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("JournalPrompt not found with id: " + id));

        existingPrompt.setPromptText(prompt.getPromptText());
        existingPrompt.setType(prompt.getType());

        return journalPromptRepository.save(existingPrompt);
    }

    @Override
    public void deletePrompt(Long id) {
        if (!journalPromptRepository.existsById(id)) {
            throw new IllegalArgumentException("JournalPrompt not found with id: " + id);
        }
        journalPromptRepository.deleteById(id);
    }
}