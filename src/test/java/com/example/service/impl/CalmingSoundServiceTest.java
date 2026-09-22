package com.example.service.impl;

import com.example.jpaRepository.CalmingSoundRepository;
import com.example.model.CalmingSound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CalmingSoundServiceTest {

    @Mock
    private CalmingSoundRepository calmingSoundRepository;

    @InjectMocks
    private CalmingSoundService calmingSoundService;

    private CalmingSound testSound;

    @BeforeEach
    public void setup() {
        testSound = new CalmingSound();
        testSound.setId(1L);
        testSound.setName("Gentle Rain");
        testSound.setAudioUrl("/sounds/rain.mp3");
        testSound.setLoopEnabled(true);
        testSound.setExercises(new HashSet<>());
    }

    @Test
    public void testGetAllCalmingSounds_ReturnsList() {
        when(calmingSoundRepository.findAllByOrderByNameAsc()).thenReturn(List.of(testSound));

        List<CalmingSound> result = calmingSoundService.getAllCalmingSounds();

        assertEquals(1, result.size());
        assertEquals("Gentle Rain", result.get(0).getName());
    }

    @Test
    public void testGetCalmingSoundById_Success() {
        when(calmingSoundRepository.findById(1L)).thenReturn(Optional.of(testSound));

        CalmingSound result = calmingSoundService.getCalmingSoundById(1L);

        assertNotNull(result);
        assertEquals("Gentle Rain", result.getName());
    }

    @Test
    public void testGetCalmingSoundById_NotFound_ThrowsException() {
        when(calmingSoundRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> calmingSoundService.getCalmingSoundById(999L));
    }

    @Test
    public void testUpdateCalmingSound_UpdatesNameAndLoop() {
        when(calmingSoundRepository.findById(1L)).thenReturn(Optional.of(testSound));
        when(calmingSoundRepository.save(any(CalmingSound.class))).thenAnswer(i -> i.getArgument(0));

        CalmingSound result = calmingSoundService.updateCalmingSound(1L, "Ocean Waves", false);

        assertEquals("Ocean Waves", result.getName());
        assertFalse(result.isLoopEnabled());
    }

    @Test
    public void testUpdateCalmingSound_NullFields_DoesNotOverwrite() {
        when(calmingSoundRepository.findById(1L)).thenReturn(Optional.of(testSound));
        when(calmingSoundRepository.save(any(CalmingSound.class))).thenAnswer(i -> i.getArgument(0));

        CalmingSound result = calmingSoundService.updateCalmingSound(1L, null, null);

        assertEquals("Gentle Rain", result.getName());
        assertTrue(result.isLoopEnabled());
    }

    @Test
    public void testDeleteCalmingSound_NoExercises_Deletes() {
        when(calmingSoundRepository.findById(1L)).thenReturn(Optional.of(testSound));
        doNothing().when(calmingSoundRepository).delete(testSound);

        calmingSoundService.deleteCalmingSound(1L);

        verify(calmingSoundRepository, times(1)).delete(testSound);
    }

    @Test
    public void testDeleteCalmingSound_WithExercises_ThrowsException() {
        testSound.getExercises().add(new com.example.model.MindfulnessExercise());
        when(calmingSoundRepository.findById(1L)).thenReturn(Optional.of(testSound));

        assertThrows(RuntimeException.class, () -> calmingSoundService.deleteCalmingSound(1L));
        verify(calmingSoundRepository, never()).delete(any());
    }

    @Test
    public void testToggleLoopSetting_TogglesFromTrueToFalse() {
        testSound.setLoopEnabled(true);
        when(calmingSoundRepository.findById(1L)).thenReturn(Optional.of(testSound));
        when(calmingSoundRepository.save(any(CalmingSound.class))).thenAnswer(i -> i.getArgument(0));

        CalmingSound result = calmingSoundService.toggleLoopSetting(1L);

        assertFalse(result.isLoopEnabled());
    }

    @Test
    public void testSearchSoundsByName_ReturnsMatches() {
        when(calmingSoundRepository.findByNameContainingIgnoreCase("rain")).thenReturn(List.of(testSound));

        List<CalmingSound> result = calmingSoundService.searchSoundsByName("rain");

        assertEquals(1, result.size());
        assertEquals("Gentle Rain", result.get(0).getName());
    }

    @Test
    public void testSearchSoundsByName_EmptySearch_ReturnsAll() {
        when(calmingSoundRepository.findAllByOrderByNameAsc()).thenReturn(List.of(testSound));

        List<CalmingSound> result = calmingSoundService.searchSoundsByName("");

        assertEquals(1, result.size());
    }

    @Test
    public void testGetSoundsForExerciseDropdown_IncludesNoSoundOption() {
        when(calmingSoundRepository.findAllByOrderByNameAsc()).thenReturn(List.of(testSound));

        List<Map<String, Object>> result = calmingSoundService.getSoundsForExerciseDropdown();

        assertEquals(2, result.size());
        assertEquals("No Sound", result.get(0).get("name"));
        assertEquals("Gentle Rain", result.get(1).get("name"));
    }

    @Test
    public void testBulkUpdateLoopSettings_UpdatesAll() {
        CalmingSound sound2 = new CalmingSound();
        sound2.setId(2L);
        sound2.setLoopEnabled(true);

        when(calmingSoundRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(testSound, sound2));
        when(calmingSoundRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        int count = calmingSoundService.bulkUpdateLoopSettings(List.of(1L, 2L), false);

        assertEquals(2, count);
        assertFalse(testSound.isLoopEnabled());
        assertFalse(sound2.isLoopEnabled());
    }

    @Test
    public void testInitializeDefaultSounds_SkipsIfAlreadyExists() {
        when(calmingSoundRepository.count()).thenReturn(5L);

        calmingSoundService.initializeDefaultSounds();

        verify(calmingSoundRepository, never()).save(any());
    }

    @Test
    public void testInitializeDefaultSounds_CreatesWhenEmpty() {
        when(calmingSoundRepository.count()).thenReturn(0L);
        when(calmingSoundRepository.save(any(CalmingSound.class))).thenAnswer(i -> i.getArgument(0));

        calmingSoundService.initializeDefaultSounds();

        verify(calmingSoundRepository, times(5)).save(any(CalmingSound.class));
    }
}
