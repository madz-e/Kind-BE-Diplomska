package com.example.controller;

import com.example.model.Affirmation;
import com.example.model.exceptions.AffirmationNotFoundException;
import com.example.model.exceptions.EmptyAffirmationTextException;
import com.example.service.impl.AffirmationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/affirmations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // For mobile app development
public class AffirmationController {

    private final AffirmationService affirmationService;

    @ExceptionHandler(AffirmationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(AffirmationNotFoundException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", e.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(EmptyAffirmationTextException.class)
    public ResponseEntity<Map<String, Object>> handleEmptyText(EmptyAffirmationTextException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", e.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", e.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * HAS TO BE CALLED FOR AFFIRMATIONS TO WORK
     * POST /api/affirmations/init
     * Initialize with default affirmations
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initializeAffirmations() {
        affirmationService.initializeDefaultAffirmations();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Default affirmations initialized");
        response.put("count", affirmationService.getAllAffirmations().size());
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/affirmations/random
     * Get a random affirmation for home page
     * Returns different affirmation each time
     * Response:
     * {
     * "success": true,
     * "id": 3,
     * "text": "My potential is limitless.",
     * "timestamp": "2024-01-15T14:30:00"
     * }
     */
    @GetMapping("/random")
    public ResponseEntity<Map<String, Object>> getRandomAffirmation() {
        Affirmation affirmation = affirmationService.getRandomAffirmation();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("id", affirmation.getId());
        response.put("text", affirmation.getAffirmationText());
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/affirmations/today
     * Get today's affirmation (same all day)
     * Response:
     * {
     * "success": true,
     * "id": 3,
     * "text": "My potential is limitless.",
     * "date": "2024-01-15",
     * "timestamp": "2024-01-15T14:30:00"
     * }
     */
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodaysAffirmation() {
        Affirmation affirmation = affirmationService.getTodaysAffirmation();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("id", affirmation.getId());
        response.put("text", affirmation.getAffirmationText());
        response.put("date", LocalDate.now().toString());
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/affirmations
     * Get all affirmations
     */
    @GetMapping
    public ResponseEntity<List<Affirmation>> getAllAffirmations() {
        return ResponseEntity.ok(affirmationService.getAllAffirmations());
    }

    /**
     * GET /api/affirmations/{id}
     * Get specific affirmation by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Affirmation> getAffirmationById(@PathVariable Long id) {
        return ResponseEntity.ok(affirmationService.getAffirmationById(id));
    }

    /**
     * POST /api/affirmations
     * Create new affirmation
     */
    @PostMapping
    public ResponseEntity<Affirmation> createAffirmation(@RequestBody Affirmation affirmation) {
        return ResponseEntity.ok(affirmationService.createAffirmation(affirmation));
    }

    /**
     * PUT /api/affirmations/{id}
     * Update affirmation text
     */
    @PutMapping("/{id}")
    public ResponseEntity<Affirmation> updateAffirmation(
            @PathVariable Long id,
            @RequestParam String text) {
        return ResponseEntity.ok(affirmationService.updateAffirmation(id, text));
    }

    /**
     * DELETE /api/affirmations/{id}
     * Delete affirmation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAffirmation(@PathVariable Long id) {
        affirmationService.deleteAffirmation(id);
        return ResponseEntity.ok().build();
    }
}