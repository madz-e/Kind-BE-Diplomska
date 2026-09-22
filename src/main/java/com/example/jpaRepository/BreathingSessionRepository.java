package com.example.jpaRepository;

import com.example.model.BreathingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BreathingSessionRepository extends JpaRepository<BreathingSession, Long> {

    // Get all breathing sessions for a user
    List<BreathingSession> findByUserId(Long userId);
}
