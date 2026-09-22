package com.example.service.impl;

import com.example.jpaRepository.BreathingSessionRepository;
import com.example.jpaRepository.UserRepository;
import com.example.model.BreathingSession;
import com.example.model.User;
import com.example.service.BreathingSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BreathingSessionServiceImpl implements BreathingSessionService {

    private final BreathingSessionRepository breathingSessionRepository;
    private final UserRepository userRepository;

    @Override
    public BreathingSession createBreathingSession(BreathingSession breathingSession) {
        // Validate required fields
        if (breathingSession.getUser() == null || breathingSession.getUser().getId() == null) {
            throw new IllegalArgumentException("User is required");
        }

        if (breathingSession.getDate() == null) {
            throw new IllegalArgumentException("Date is required");
        }

        if (breathingSession.getCycles() == null || breathingSession.getCycles() < 1) {
            throw new IllegalArgumentException("Cycles must be at least 1");
        }

        // Validate user exists
        User user = userRepository.findById(breathingSession.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        breathingSession.setUser(user);

        return breathingSessionRepository.save(breathingSession);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BreathingSession> findById(Long id) {
        return breathingSessionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BreathingSession> findByUserId(Long userId) {
        return breathingSessionRepository.findByUserId(userId);
    }
}
