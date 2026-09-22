package com.example.service.impl;

import com.example.jpaRepository.EmotionRepository;
import com.example.model.Emotion;
import com.example.model.enumerations.MoodCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmotionServiceTest {

    @Mock
    private EmotionRepository emotionRepository;

    @InjectMocks
    private EmotionServiceImpl emotionService;

    private Emotion testEmotion;

    @BeforeEach
    public void setup() {
        testEmotion = new Emotion();
        testEmotion.setId(1L);
        testEmotion.setName("Happy");
        testEmotion.setMoodCategory(MoodCategory.PLEASANT);
    }

    @Test
    public void testGetAllEmotions_ReturnsList() {
        when(emotionRepository.findAll()).thenReturn(List.of(testEmotion));

        List<Emotion> result = emotionService.getAllEmotions();

        assertEquals(1, result.size());
        assertEquals("Happy", result.get(0).getName());
    }

    @Test
    public void testFindById_ReturnsEmotion() {
        when(emotionRepository.findById(1L)).thenReturn(Optional.of(testEmotion));

        Optional<Emotion> result = emotionService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(MoodCategory.PLEASANT, result.get().getMoodCategory());
    }

    @Test
    public void testFindByMoodCategory_ReturnsList() {
        when(emotionRepository.findByMoodCategory(MoodCategory.PLEASANT)).thenReturn(List.of(testEmotion));

        List<Emotion> result = emotionService.findByMoodCategory(MoodCategory.PLEASANT);

        assertEquals(1, result.size());
    }

    @Test
    public void testCreateEmotion_Success() {
        when(emotionRepository.save(any(Emotion.class))).thenReturn(testEmotion);

        Emotion result = emotionService.createEmotion(testEmotion);

        assertNotNull(result);
        assertEquals("Happy", result.getName());
    }

    @Test
    public void testUpdateEmotion_Success() {
        Emotion updated = new Emotion();
        updated.setName("Joyful");
        updated.setMoodCategory(MoodCategory.VERY_PLEASANT);

        when(emotionRepository.findById(1L)).thenReturn(Optional.of(testEmotion));
        when(emotionRepository.save(any(Emotion.class))).thenAnswer(i -> i.getArgument(0));

        Emotion result = emotionService.updateEmotion(1L, updated);

        assertEquals("Joyful", result.getName());
        assertEquals(MoodCategory.VERY_PLEASANT, result.getMoodCategory());
    }

    @Test
    public void testUpdateEmotion_NotFound_ThrowsException() {
        when(emotionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                emotionService.updateEmotion(999L, testEmotion));
    }

    @Test
    public void testDeleteEmotion_Success() {
        when(emotionRepository.existsById(1L)).thenReturn(true);
        doNothing().when(emotionRepository).deleteById(1L);

        emotionService.deleteEmotion(1L);

        verify(emotionRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteEmotion_NotFound_ThrowsException() {
        when(emotionRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> emotionService.deleteEmotion(999L));
    }
}
