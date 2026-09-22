package com.example.controller;

import com.example.model.Emotion;
import com.example.model.enumerations.MoodCategory;
import com.example.service.EmotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/emotions")
@RequiredArgsConstructor
public class EmotionController {

    private final EmotionService emotionService;

    @GetMapping
    public ResponseEntity<List<Emotion>> getAllEmotions() {
        List<Emotion> emotions = emotionService.getAllEmotions();
        return ResponseEntity.ok(emotions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Emotion> getEmotionById(@PathVariable Long id) {
        Optional<Emotion> emotion = emotionService.findById(id);
        return emotion.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET emotions by mood category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Emotion>> getEmotionsByCategory(
            @PathVariable MoodCategory category) {

        List<Emotion> emotions = emotionService.findByMoodCategory(category);
        return ResponseEntity.ok(emotions);
    }

    @PostMapping
    public ResponseEntity<Emotion> createEmotion(@RequestBody Emotion emotion) {
        Emotion savedEmotion = emotionService.createEmotion(emotion);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEmotion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Emotion> updateEmotion(
            @PathVariable Long id,
            @RequestBody Emotion emotion) {

        Emotion updatedEmotion = emotionService.updateEmotion(id, emotion);
        return ResponseEntity.ok(updatedEmotion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmotion(@PathVariable Long id) {
        emotionService.deleteEmotion(id);
        return ResponseEntity.noContent().build();
    }

}
