package com.example.service;

import com.example.model.JournalEntry;
import com.example.model.enumerations.EntryType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface JournalEntryService {
    JournalEntry createJournalEntry(JournalEntry journalEntry);

    Optional<JournalEntry> findById(Long id);

    List<JournalEntry> findByUserId(Long userId);

    List<JournalEntry> findByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    List<JournalEntry> findByUserIdAndType(Long userId, EntryType type);

    JournalEntry updateJournalEntry(Long id, JournalEntry journalEntry);

    void deleteJournalEntry(Long id);

    long countByUserId(Long userId);

    Map<String, Long> getWordFrequency(Long userId, LocalDateTime startDate, LocalDateTime endDate, int topN);
}