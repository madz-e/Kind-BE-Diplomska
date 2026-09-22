package com.example.controller;

import com.example.model.JournalPrompt;
import com.example.model.enumerations.JournalPromptType;
import com.example.service.JournalPromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/journal-prompts")
@RequiredArgsConstructor
public class JournalPromptController {

    private final JournalPromptService journalPromptService;

    @GetMapping
    public ResponseEntity<List<JournalPrompt>> getAllPrompts() {
        List<JournalPrompt> prompts = journalPromptService.getAllPrompts();
        return ResponseEntity.ok(prompts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalPrompt> getPromptById(@PathVariable Long id) {
        Optional<JournalPrompt> prompt = journalPromptService.findById(id);
        return prompt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET prompts by type (GENERAL or ANT_EXERCISE)
    @GetMapping("/type/{type}")
    public ResponseEntity<List<JournalPrompt>> getPromptsByType(
            @PathVariable JournalPromptType type) {

        List<JournalPrompt> prompts = journalPromptService.findByType(type);
        return ResponseEntity.ok(prompts);
    }

    // TODO: Admin only
    @PostMapping
    public ResponseEntity<JournalPrompt> createPrompt(@RequestBody JournalPrompt prompt) {
        JournalPrompt savedPrompt = journalPromptService.createPrompt(prompt);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPrompt);
    }

    // TODO: Admin only
    @PutMapping("/{id}")
    public ResponseEntity<JournalPrompt> updatePrompt(
            @PathVariable Long id,
            @RequestBody JournalPrompt prompt) {

        JournalPrompt updatedPrompt = journalPromptService.updatePrompt(id, prompt);
        return ResponseEntity.ok(updatedPrompt);
    }

    // TODO: Admin only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrompt(@PathVariable Long id) {
        journalPromptService.deletePrompt(id);
        return ResponseEntity.noContent().build();
    }
}
