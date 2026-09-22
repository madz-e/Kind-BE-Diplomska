package com.example.service;

import com.example.model.BreathingSession;

import java.util.List;
import java.util.Optional;

public interface BreathingSessionService {
    BreathingSession createBreathingSession(BreathingSession breathingSession);

    Optional<BreathingSession> findById(Long id);

    List<BreathingSession> findByUserId(Long userId);
}
