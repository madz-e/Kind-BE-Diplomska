package com.example.service.impl;

import com.example.jpaRepository.EmotionRepository;
import com.example.jpaRepository.MoodEntryRepository;
import com.example.jpaRepository.MoodFactorRepository;
import com.example.jpaRepository.UserRepository;
import com.example.model.Emotion;
import com.example.model.MoodEntry;
import com.example.model.User;
import com.example.model.enumerations.MoodCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MoodEntryServiceTest {

    @Mock private MoodEntryRepository moodEntryRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmotionRepository emotionRepository;
    @Mock private MoodFactorRepository moodFactorRepository;

    @InjectMocks
    private MoodEntryServiceImpl moodEntryService;

    private User testUser;
    private MoodEntry testEntry;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);

        testEntry = new MoodEntry();
        testEntry.setId(1L);
        testEntry.setUser(testUser);
        testEntry.setDate(LocalDate.now());
        testEntry.setMoodValue(7);
        testEntry.setNote("Feeling good");
    }

    @Test
    public void testCreateMoodEntry_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(moodEntryRepository.save(any(MoodEntry.class))).thenReturn(testEntry);

        MoodEntry result = moodEntryService.createMoodEntry(testEntry);

        assertNotNull(result);
        assertEquals(7, result.getMoodValue());
        assertEquals(MoodCategory.PLEASANT, result.getMoodCategory());
    }

    @Test
    public void testCreateMoodEntry_UserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> moodEntryService.createMoodEntry(testEntry));
        verify(moodEntryRepository, never()).save(any());
    }

    @Test
    public void testCreateMoodEntry_WithEmotions_ValidatesEach() {
        Emotion emotion = new Emotion();
        emotion.setId(1L);
        emotion.setName("Happy");
        Set<Emotion> emotions = new HashSet<>();
        emotions.add(emotion);
        testEntry.setSelectedEmotions(emotions);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(emotionRepository.findById(1L)).thenReturn(Optional.of(emotion));
        when(moodEntryRepository.save(any(MoodEntry.class))).thenReturn(testEntry);

        MoodEntry result = moodEntryService.createMoodEntry(testEntry);

        assertNotNull(result);
        verify(emotionRepository, times(1)).findById(1L);
    }

    @Test
    public void testCreateMoodEntry_InvalidEmotionId_ThrowsException() {
        Emotion emotion = new Emotion();
        emotion.setId(999L);
        Set<Emotion> emotions = new HashSet<>();
        emotions.add(emotion);
        testEntry.setSelectedEmotions(emotions);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(emotionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> moodEntryService.createMoodEntry(testEntry));
    }

    @Test
    public void testFindById_Success() {
        when(moodEntryRepository.findById(1L)).thenReturn(Optional.of(testEntry));

        Optional<MoodEntry> result = moodEntryService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(7, result.get().getMoodValue());
    }

    @Test
    public void testFindByUserId_ReturnsList() {
        when(moodEntryRepository.findByUserId(1L)).thenReturn(List.of(testEntry));

        List<MoodEntry> result = moodEntryService.findByUserId(1L);

        assertEquals(1, result.size());
    }

    @Test
    public void testFindByUserIdAndDate_ReturnsEntry() {
        when(moodEntryRepository.findByUserIdAndDate(1L, LocalDate.now()))
                .thenReturn(Optional.of(testEntry));

        Optional<MoodEntry> result = moodEntryService.findByUserIdAndDate(1L, LocalDate.now());

        assertTrue(result.isPresent());
    }

    @Test
    public void testUpdateMoodEntry_Success() {
        MoodEntry updated = new MoodEntry();
        updated.setMoodValue(9);
        updated.setNote("Feeling great");

        when(moodEntryRepository.findById(1L)).thenReturn(Optional.of(testEntry));
        when(moodEntryRepository.save(any(MoodEntry.class))).thenAnswer(i -> i.getArgument(0));

        MoodEntry result = moodEntryService.updateMoodEntry(1L, updated);

        assertEquals(9, result.getMoodValue());
        assertEquals(MoodCategory.VERY_PLEASANT, result.getMoodCategory());
        assertEquals("Feeling great", result.getNote());
    }

    @Test
    public void testUpdateMoodEntry_NotFound_ThrowsException() {
        when(moodEntryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                moodEntryService.updateMoodEntry(999L, testEntry));
    }

    @Test
    public void testDeleteMoodEntry_Success() {
        when(moodEntryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(moodEntryRepository).deleteById(1L);

        moodEntryService.deleteMoodEntry(1L);

        verify(moodEntryRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteMoodEntry_NotFound_ThrowsException() {
        when(moodEntryRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> moodEntryService.deleteMoodEntry(999L));
    }

    @Test
    public void testCalculateAverageMood_ReturnsValue() {
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();

        when(moodEntryRepository.calculateAverageMood(1L, start, end)).thenReturn(7.5);

        Double result = moodEntryService.calculateAverageMood(1L, start, end);

        assertEquals(7.5, result);
    }
}
