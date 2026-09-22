package com.example.controller;

import com.example.model.MoodFactor;
import com.example.service.MoodFactorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mood-factors")
@RequiredArgsConstructor
public class MoodFactorController {

    private final MoodFactorService moodFactorService;

    @GetMapping
    public ResponseEntity<List<MoodFactor>> getAllMoodFactors() {
        List<MoodFactor> moodFactors = moodFactorService.getAllMoodFactors();
        return ResponseEntity.ok(moodFactors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MoodFactor> getMoodFactorById(@PathVariable Long id) {
        Optional<MoodFactor> moodFactor = moodFactorService.findById(id);
        return moodFactor.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MoodFactor> createMoodFactor(@RequestBody MoodFactor moodFactor) {
        MoodFactor savedMoodFactor = moodFactorService.createMoodFactor(moodFactor);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMoodFactor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MoodFactor> updateMoodFactor(
            @PathVariable Long id,
            @RequestBody MoodFactor moodFactor) {

        MoodFactor updatedMoodFactor = moodFactorService.updateMoodFactor(id, moodFactor);
        return ResponseEntity.ok(updatedMoodFactor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMoodFactor(@PathVariable Long id) {
        moodFactorService.deleteMoodFactor(id);
        return ResponseEntity.noContent().build();
    }


}
