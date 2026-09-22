package com.example.controller;

import com.example.model.BreathingSession;
import com.example.service.BreathingSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/breathing-sessions")
@RequiredArgsConstructor
public class BreathingSessionController {

    private final BreathingSessionService breathingSessionService;

    // GET all breathing sessions for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BreathingSession>> getUserBreathingSessions(@PathVariable Long userId) {
        List<BreathingSession> sessions = breathingSessionService.findByUserId(userId);
        return ResponseEntity.ok(sessions);
    }

    // POST - Create new breathing session
    @PostMapping
    public ResponseEntity<?> createBreathingSession(@RequestBody BreathingSession breathingSession) {
        try {
            BreathingSession savedSession = breathingSessionService.createBreathingSession(breathingSession);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSession);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
