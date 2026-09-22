package com.example.service.impl;

import com.example.jpaRepository.BreathingSessionRepository;
import com.example.jpaRepository.UserRepository;
import com.example.model.BreathingSession;
import com.example.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BreathingSessionServiceTest {

    @Mock private BreathingSessionRepository breathingSessionRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private BreathingSessionServiceImpl breathingSessionService;

    private User testUser;
    private BreathingSession testSession;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);

        testSession = new BreathingSession();
        testSession.setId(1L);
        testSession.setUser(testUser);
        testSession.setDate(LocalDate.now());
        testSession.setExerciseType("box");
        testSession.setCycles(4);
    }

    @Test
    public void testCreateBreathingSession_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(breathingSessionRepository.save(any(BreathingSession.class))).thenReturn(testSession);

        BreathingSession result = breathingSessionService.createBreathingSession(testSession);

        assertNotNull(result);
        assertEquals("box", result.getExerciseType());
        assertEquals(4, result.getCycles());
    }

    @Test
    public void testCreateBreathingSession_UserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> breathingSessionService.createBreathingSession(testSession));
        verify(breathingSessionRepository, never()).save(any());
    }

    @Test
    public void testCreateBreathingSession_MissingUser_ThrowsException() {
        testSession.setUser(null);

        assertThrows(IllegalArgumentException.class, () -> breathingSessionService.createBreathingSession(testSession));
        verify(breathingSessionRepository, never()).save(any());
    }

    @Test
    public void testCreateBreathingSession_MissingDate_ThrowsException() {
        testSession.setDate(null);

        assertThrows(IllegalArgumentException.class, () -> breathingSessionService.createBreathingSession(testSession));
        verify(breathingSessionRepository, never()).save(any());
    }

    @Test
    public void testCreateBreathingSession_CyclesLessThanOne_ThrowsException() {
        testSession.setCycles(0);

        assertThrows(IllegalArgumentException.class, () -> breathingSessionService.createBreathingSession(testSession));
        verify(breathingSessionRepository, never()).save(any());
    }

    @Test
    public void testCreateBreathingSession_MissingCycles_ThrowsException() {
        testSession.setCycles(null);

        assertThrows(IllegalArgumentException.class, () -> breathingSessionService.createBreathingSession(testSession));
        verify(breathingSessionRepository, never()).save(any());
    }

    @Test
    public void testFindById_Success() {
        when(breathingSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

        Optional<BreathingSession> result = breathingSessionService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("box", result.get().getExerciseType());
    }

    @Test
    public void testFindByUserId_ReturnsList() {
        when(breathingSessionRepository.findByUserId(1L)).thenReturn(List.of(testSession));

        List<BreathingSession> result = breathingSessionService.findByUserId(1L);

        assertEquals(1, result.size());
    }
}
