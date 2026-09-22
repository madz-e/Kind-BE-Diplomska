package com.example.service;

import com.example.model.JournalPrompt;
import com.example.model.enumerations.JournalPromptType;

import java.util.List;
import java.util.Optional;

public interface JournalPromptService {
    List<JournalPrompt> getAllPrompts();

    Optional<JournalPrompt> findById(Long id);

    List<JournalPrompt> findByType(JournalPromptType type);

    JournalPrompt createPrompt(JournalPrompt prompt);

    JournalPrompt updatePrompt(Long id, JournalPrompt prompt);

    void deletePrompt(Long id);
}