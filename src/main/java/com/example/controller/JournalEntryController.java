package com.example.controller;

import com.example.model.JournalEntry;
import com.example.model.enumerations.EntryType;
import com.example.service.JournalEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/journal-entries")
@RequiredArgsConstructor
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<JournalEntry>> getUserEntries(@PathVariable Long userId) {
        List<JournalEntry> entries = journalEntryService.findByUserId(userId);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalEntry> getEntryById(@PathVariable Long id) {
        Optional<JournalEntry> entry = journalEntryService.findById(id);
        return entry.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET entries by user and date range
    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<JournalEntry>> getEntriesByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<JournalEntry> entries = journalEntryService.findByUserIdAndDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(entries);
    }

    // GET entries by user and type (BLANK or PROMPT_BASED)
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<JournalEntry>> getEntriesByType(
            @PathVariable Long userId,
            @PathVariable EntryType type) {

        List<JournalEntry> entries = journalEntryService.findByUserIdAndType(userId, type);
        return ResponseEntity.ok(entries);
    }

    // GET entry count for a user
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getEntryCount(@PathVariable Long userId) {
        long count = journalEntryService.countByUserId(userId);
        return ResponseEntity.ok(count);
    }

    @PostMapping
    public ResponseEntity<?> createEntry(@RequestBody JournalEntry entry) {
        try {
            JournalEntry savedEntry = journalEntryService.createJournalEntry(entry);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEntry);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEntry(
            @PathVariable Long id,
            @RequestBody JournalEntry entry) {

        try {
            JournalEntry updatedEntry = journalEntryService.updateJournalEntry(id, entry);
            return ResponseEntity.ok(updatedEntry);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        try {
            journalEntryService.deleteJournalEntry(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }


}
