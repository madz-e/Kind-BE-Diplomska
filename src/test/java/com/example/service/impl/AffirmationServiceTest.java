package com.example.service.impl;

import com.example.jpaRepository.AffirmationRepository;
import com.example.model.Affirmation;
import com.example.model.Reminder;
import com.example.model.exceptions.AffirmationNotFoundException;
import com.example.model.exceptions.EmptyAffirmationTextException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AffirmationServiceTest {

    @Mock
    private AffirmationRepository affirmationRepository;

    @InjectMocks
    private AffirmationService affirmationService;

    private Affirmation testAffirmation;

    @BeforeEach
    void setUp() {
        testAffirmation = new Affirmation();
        testAffirmation.setId(1L);
        testAffirmation.setAffirmationText("Test affirmation");
        testAffirmation.setReminders(new HashSet<>());
    }

    // ========== getRandomAffirmation TESTS ==========

    @Test
    void testGetRandomAffirmation_Success() {
        when(affirmationRepository.findRandom()).thenReturn(Optional.of(testAffirmation));

        Affirmation result = affirmationService.getRandomAffirmation();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAffirmationText()).isEqualTo("Test affirmation");
        verify(affirmationRepository, times(1)).findRandom();
    }

    @Test
    void testGetRandomAffirmation_WhenEmpty_InitializesAndReturns() {
        Affirmation savedAffirmation = new Affirmation();
        savedAffirmation.setId(2L);
        savedAffirmation.setAffirmationText("After init");
        savedAffirmation.setReminders(new HashSet<>());

        when(affirmationRepository.findRandom())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(savedAffirmation));
        when(affirmationRepository.count()).thenReturn(0L);

        Affirmation result = affirmationService.getRandomAffirmation();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getAffirmationText()).isEqualTo("After init");
        verify(affirmationRepository, times(2)).findRandom();
        verify(affirmationRepository, times(2)).count();
    }

    // ========== getAllAffirmations TESTS ==========

    @Test
    void testGetAllAffirmations_Success() {
        List<Affirmation> affirmations = Arrays.asList(testAffirmation);
        when(affirmationRepository.findAll()).thenReturn(affirmations);

        List<Affirmation> result = affirmationService.getAllAffirmations();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(affirmationRepository, times(1)).findAll();
    }

    @Test
    void testGetAllAffirmations_WhenEmpty_Initializes() {
        Affirmation newAffirmation = new Affirmation();
        newAffirmation.setId(2L);
        newAffirmation.setAffirmationText("After init");
        newAffirmation.setReminders(new HashSet<>());

        when(affirmationRepository.findAll())
                .thenReturn(Arrays.asList())
                .thenReturn(Arrays.asList(newAffirmation));

        List<Affirmation> result = affirmationService.getAllAffirmations();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        verify(affirmationRepository, times(2)).findAll();
    }

    // ========== initializeDefaultAffirmations TESTS ==========

    @Test
    void testInitializeDefaultAffirmations_OnlyWhenEmpty() {
        when(affirmationRepository.count()).thenReturn(0L);

        affirmationService.initializeDefaultAffirmations();

        verify(affirmationRepository, atLeastOnce()).save(any(Affirmation.class));
        verify(affirmationRepository, times(1)).count();
    }

    @Test
    void testInitializeDefaultAffirmations_SkipsWhenNotEmpty() {
        when(affirmationRepository.count()).thenReturn(10L);

        affirmationService.initializeDefaultAffirmations();

        verify(affirmationRepository, never()).save(any(Affirmation.class));
        verify(affirmationRepository, times(1)).count();
    }

    // ========== getTodaysAffirmation TESTS ==========

    @Test
    void testGetTodaysAffirmation_Success() {
        List<Affirmation> affirmations = Arrays.asList(testAffirmation);
        when(affirmationRepository.findAll()).thenReturn(affirmations);

        Affirmation result = affirmationService.getTodaysAffirmation();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAffirmationText()).isEqualTo("Test affirmation");
        verify(affirmationRepository, times(1)).findAll();
    }

    @Test
    void testGetTodaysAffirmation_WhenEmpty_Initializes() {
        Affirmation newAffirmation = new Affirmation();
        newAffirmation.setId(2L);
        newAffirmation.setAffirmationText("After init");
        newAffirmation.setReminders(new HashSet<>());

        when(affirmationRepository.findAll())
                .thenReturn(Arrays.asList())
                .thenReturn(Arrays.asList(newAffirmation));

        Affirmation result = affirmationService.getTodaysAffirmation();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getAffirmationText()).isEqualTo("After init");
        verify(affirmationRepository, times(2)).findAll();
    }

    // ========== getAffirmationById TESTS ==========

    @Test
    void testGetAffirmationById_Success() {
        when(affirmationRepository.findById(1L)).thenReturn(Optional.of(testAffirmation));

        Affirmation result = affirmationService.getAffirmationById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAffirmationText()).isEqualTo("Test affirmation");
        verify(affirmationRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAffirmationById_NotFound_ThrowsAffirmationNotFoundException() {
        when(affirmationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> affirmationService.getAffirmationById(999L))
                .isInstanceOf(AffirmationNotFoundException.class)
                .hasMessageContaining("Affirmation with id: 999 was not found");

        verify(affirmationRepository, times(1)).findById(999L);
    }

    // ========== createAffirmation TESTS ==========

    @Test
    void testCreateAffirmation_Success() {
        Affirmation newAffirmation = new Affirmation();
        newAffirmation.setAffirmationText("New affirmation");
        newAffirmation.setReminders(new HashSet<>());

        Affirmation savedAffirmation = new Affirmation();
        savedAffirmation.setId(5L);
        savedAffirmation.setAffirmationText("New affirmation");
        savedAffirmation.setReminders(new HashSet<>());

        when(affirmationRepository.save(any(Affirmation.class))).thenReturn(savedAffirmation);

        Affirmation result = affirmationService.createAffirmation(newAffirmation);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getAffirmationText()).isEqualTo("New affirmation");
        verify(affirmationRepository, times(1)).save(any(Affirmation.class));
    }

    @Test
    void testCreateAffirmation_WithEmptyText_ThrowsEmptyAffirmationTextException() {
        Affirmation invalidAffirmation = new Affirmation();
        invalidAffirmation.setAffirmationText("");
        invalidAffirmation.setReminders(new HashSet<>());

        assertThatThrownBy(() -> affirmationService.createAffirmation(invalidAffirmation))
                .isInstanceOf(EmptyAffirmationTextException.class)
                .hasMessageContaining("Affirmation text cannot be null or empty");

        verify(affirmationRepository, never()).save(any());
    }

    @Test
    void testCreateAffirmation_WithNullText_ThrowsEmptyAffirmationTextException() {
        Affirmation invalidAffirmation = new Affirmation();
        invalidAffirmation.setAffirmationText(null);
        invalidAffirmation.setReminders(new HashSet<>());

        assertThatThrownBy(() -> affirmationService.createAffirmation(invalidAffirmation))
                .isInstanceOf(EmptyAffirmationTextException.class)
                .hasMessageContaining("Affirmation text cannot be null or empty");

        verify(affirmationRepository, never()).save(any());
    }

    @Test
    void testCreateAffirmation_WithWhitespaceOnly_ThrowsEmptyAffirmationTextException() {
        Affirmation invalidAffirmation = new Affirmation();
        invalidAffirmation.setAffirmationText("   ");
        invalidAffirmation.setReminders(new HashSet<>());

        assertThatThrownBy(() -> affirmationService.createAffirmation(invalidAffirmation))
                .isInstanceOf(EmptyAffirmationTextException.class)
                .hasMessageContaining("Affirmation text cannot be null or empty");

        verify(affirmationRepository, never()).save(any());
    }

    // ========== updateAffirmation TESTS ==========

    @Test
    void testUpdateAffirmation_Success() {
        when(affirmationRepository.findById(1L)).thenReturn(Optional.of(testAffirmation));
        when(affirmationRepository.save(any(Affirmation.class))).thenReturn(testAffirmation);

        Affirmation result = affirmationService.updateAffirmation(1L, "Updated text");

        assertThat(result).isNotNull();
        assertThat(result.getAffirmationText()).isEqualTo("Updated text");
        verify(affirmationRepository, times(1)).findById(1L);
        verify(affirmationRepository, times(1)).save(any(Affirmation.class));
    }

    @Test
    void testUpdateAffirmation_NotFound_ThrowsAffirmationNotFoundException() {
        when(affirmationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> affirmationService.updateAffirmation(999L, "New text"))
                .isInstanceOf(AffirmationNotFoundException.class)
                .hasMessageContaining("Affirmation with id: 999 was not found");

        verify(affirmationRepository, times(1)).findById(999L);
        verify(affirmationRepository, never()).save(any());
    }

    @Test
    void testUpdateAffirmation_WithEmptyText_ThrowsEmptyAffirmationTextException() {
        assertThatThrownBy(() -> affirmationService.updateAffirmation(1L, ""))
                .isInstanceOf(EmptyAffirmationTextException.class)
                .hasMessageContaining("Affirmation text cannot be null or empty");

        verify(affirmationRepository, never()).findById(any());
        verify(affirmationRepository, never()).save(any());
    }

    @Test
    void testUpdateAffirmation_WithNullText_ThrowsEmptyAffirmationTextException() {
        assertThatThrownBy(() -> affirmationService.updateAffirmation(1L, null))
                .isInstanceOf(EmptyAffirmationTextException.class)
                .hasMessageContaining("Affirmation text cannot be null or empty");

        verify(affirmationRepository, never()).findById(any());
        verify(affirmationRepository, never()).save(any());
    }

    @Test
    void testUpdateAffirmation_WithWhitespaceOnly_ThrowsEmptyAffirmationTextException() {
        assertThatThrownBy(() -> affirmationService.updateAffirmation(1L, "   "))
                .isInstanceOf(EmptyAffirmationTextException.class)
                .hasMessageContaining("Affirmation text cannot be null or empty");

        verify(affirmationRepository, never()).findById(any());
        verify(affirmationRepository, never()).save(any());
    }

    // ========== deleteAffirmation TESTS ==========

    @Test
    void testDeleteAffirmation_Success() {
        when(affirmationRepository.findById(1L)).thenReturn(Optional.of(testAffirmation));
        doNothing().when(affirmationRepository).deleteById(1L);

        affirmationService.deleteAffirmation(1L);

        verify(affirmationRepository, times(1)).findById(1L);
        verify(affirmationRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteAffirmation_NotFound_ThrowsAffirmationNotFoundException() {
        when(affirmationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> affirmationService.deleteAffirmation(999L))
                .isInstanceOf(AffirmationNotFoundException.class)
                .hasMessageContaining("Affirmation with id: 999 was not found");

        verify(affirmationRepository, times(1)).findById(999L);
        verify(affirmationRepository, never()).deleteById(any());
    }

    // ========== init TESTS ==========

    @Test
    void testInit_WhenDatabaseEmpty_Initializes() {
        when(affirmationRepository.count()).thenReturn(0L);

        affirmationService.init();

        verify(affirmationRepository, times(2)).count();
        verify(affirmationRepository, atLeastOnce()).save(any(Affirmation.class));
    }

    @Test
    void testInit_WhenDatabaseHasData_SkipsInitialization() {
        when(affirmationRepository.count()).thenReturn(10L);

        affirmationService.init();

        verify(affirmationRepository, times(1)).count();
        verify(affirmationRepository, never()).save(any(Affirmation.class));
    }
}