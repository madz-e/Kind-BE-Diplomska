package com.example.model;

import com.example.model.enumerations.EntryType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at",nullable = false)
    private LocalDate createdAt;

    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType type;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "moodEntries", "journalEntries", "habits", "reminders", "breathingSessions", "password", "authorities"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "journalEntry", "moodEntries", "journalEntries", "habits", "reminders", "breathingSessions", "selectedEmotions", "selectedFactors"})
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mood_entry_id", unique = true)
    private MoodEntry moodEntry;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "journalEntries"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id")
    private JournalPrompt journalPrompt;

    // Structured payload for special multi-field prompts (e.g. CBT thought record).
    // Holds JSON; null for ordinary entries.
    @Column(columnDefinition = "TEXT")
    private String structuredData;


}