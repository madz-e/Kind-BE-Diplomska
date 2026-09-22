package com.example.controller;

import com.example.model.MoodEntry;
import com.example.model.enumerations.MoodCategory;
import com.example.service.MoodEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mood-entries")
@RequiredArgsConstructor
public class MoodEntryController {

    private final MoodEntryService moodEntryService;

    // GET all mood entries for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MoodEntry>> getUserMoodEntries(@PathVariable Long userId) {
        List<MoodEntry> entries = moodEntryService.findByUserId(userId);
        return ResponseEntity.ok(entries);
    }

    // GET mood entry by ID
    @GetMapping("/{id}")
    public ResponseEntity<MoodEntry> getMoodEntryById(@PathVariable Long id) {
        Optional<MoodEntry> entry = moodEntryService.findById(id);
        return entry.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET mood entry by user and specific date
    @GetMapping("/user/{userId}/date/{date}")
    public ResponseEntity<MoodEntry> getMoodEntryByDate(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Optional<MoodEntry> entry = moodEntryService.findByUserIdAndDate(userId, date);
        return entry.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET mood entries by user and date range
    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<MoodEntry>> getMoodEntriesByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<MoodEntry> entries = moodEntryService.findByUserIdAndDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(entries);
    }

    // GET mood entries by user and category
    @GetMapping("/user/{userId}/category/{category}")
    public ResponseEntity<List<MoodEntry>> getMoodEntriesByCategory(
            @PathVariable Long userId,
            @PathVariable MoodCategory category) {

        List<MoodEntry> entries = moodEntryService.findByUserIdAndMoodCategory(userId, category);
        return ResponseEntity.ok(entries);
    }

    // GET mood entries by user and year/month (calendar view)
    @GetMapping("/user/{userId}/calendar/{year}/{month}")
    public ResponseEntity<List<MoodEntry>> getMoodEntriesByMonth(
            @PathVariable Long userId,
            @PathVariable int year,
            @PathVariable int month) {

        List<MoodEntry> entries = moodEntryService.findByUserIdAndYearMonth(userId, year, month);
        return ResponseEntity.ok(entries);
    }

    // GET average mood for date range
    @GetMapping("/user/{userId}/average")
    public ResponseEntity<Double> getAverageMood(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Double average = moodEntryService.calculateAverageMood(userId, startDate, endDate);
        return ResponseEntity.ok(average);
    }

    // POST - Create new mood entry
    @PostMapping
    public ResponseEntity<?> createMoodEntry(@RequestBody MoodEntry moodEntry) {
        try {
            MoodEntry savedEntry = moodEntryService.createMoodEntry(moodEntry);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEntry);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT - Update mood entry
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMoodEntry(
            @PathVariable Long id,
            @RequestBody MoodEntry moodEntry) {

        try {
            MoodEntry updatedEntry = moodEntryService.updateMoodEntry(id, moodEntry);
            return ResponseEntity.ok(updatedEntry);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        }
    }

    // DELETE mood entry
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMoodEntry(@PathVariable Long id) {
        try {
            moodEntryService.deleteMoodEntry(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }


}
