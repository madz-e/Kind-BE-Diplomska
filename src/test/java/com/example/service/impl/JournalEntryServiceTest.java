package com.example.service.impl;

import com.example.jpaRepository.JournalEntryRepository;
import com.example.jpaRepository.JournalPromptRepository;
import com.example.jpaRepository.MoodEntryRepository;
import com.example.jpaRepository.UserRepository;
import com.example.model.JournalEntry;
import com.example.model.JournalPrompt;
import com.example.model.User;
import com.example.model.enumerations.EntryType;
import com.example.model.enumerations.JournalPromptType;
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
public class JournalEntryServiceTest {

    @Mock private JournalEntryRepository journalEntryRepository;
    @Mock private UserRepository userRepository;
    @Mock private JournalPromptRepository journalPromptRepository;
    @Mock private MoodEntryRepository moodEntryRepository;

    @InjectMocks
    private JournalEntryServiceImpl journalEntryService;

    private User testUser;
    private JournalEntry blankEntry;
    private JournalEntry promptEntry;
    private JournalPrompt testPrompt;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);

        testPrompt = new JournalPrompt();
        testPrompt.setId(1L);
        testPrompt.setPromptText("What are you grateful for?");
        testPrompt.setType(JournalPromptType.GENERAL);

        blankEntry = new JournalEntry();
        blankEntry.setId(1L);
        blankEntry.setUser(testUser);
        blankEntry.setTitle("My Day");
        blankEntry.setContent("Today was great.");
        blankEntry.setType(EntryType.BLANK);
        blankEntry.setCreatedAt(LocalDate.now());

        promptEntry = new JournalEntry();
        promptEntry.setId(2L);
        promptEntry.setUser(testUser);
        promptEntry.setContent("I am grateful for my family.");
        promptEntry.setType(EntryType.PROMPT_BASED);
        promptEntry.setJournalPrompt(testPrompt);
        promptEntry.setCreatedAt(LocalDate.now());
    }

    @Test
    public void testCreateBlankEntry_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(blankEntry);

        JournalEntry result = journalEntryService.createJournalEntry(blankEntry);

        assertNotNull(result);
        assertEquals(EntryType.BLANK, result.getType());
        assertNull(result.getJournalPrompt());
    }

    @Test
    public void testCreateBlankEntry_NoTitle_ThrowsException() {
        blankEntry.setTitle(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () ->
                journalEntryService.createJournalEntry(blankEntry));
    }

    @Test
    public void testCreateBlankEntry_EmptyContent_ThrowsException() {
        blankEntry.setContent("");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () ->
                journalEntryService.createJournalEntry(blankEntry));
    }

    @Test
    public void testCreatePromptEntry_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(journalPromptRepository.findById(1L)).thenReturn(Optional.of(testPrompt));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(promptEntry);

        JournalEntry result = journalEntryService.createJournalEntry(promptEntry);

        assertNotNull(result);
        assertEquals(EntryType.PROMPT_BASED, result.getType());
    }

    @Test
    public void testCreatePromptEntry_NoPrompt_ThrowsException() {
        promptEntry.setJournalPrompt(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () ->
                journalEntryService.createJournalEntry(promptEntry));
    }

    @Test
    public void testCreateEntry_NullType_ThrowsException() {
        blankEntry.setType(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () ->
                journalEntryService.createJournalEntry(blankEntry));
    }

    @Test
    public void testCreateEntry_SetsCreatedAtIfNull() {
        blankEntry.setCreatedAt(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(i -> i.getArgument(0));

        JournalEntry result = journalEntryService.createJournalEntry(blankEntry);

        assertEquals(LocalDate.now(), result.getCreatedAt());
    }

    @Test
    public void testFindById_ReturnsEntry() {
        when(journalEntryRepository.findById(1L)).thenReturn(Optional.of(blankEntry));

        Optional<JournalEntry> result = journalEntryService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(EntryType.BLANK, result.get().getType());
    }

    @Test
    public void testFindByUserId_ReturnsList() {
        when(journalEntryRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(blankEntry, promptEntry));

        List<JournalEntry> result = journalEntryService.findByUserId(1L);

        assertEquals(2, result.size());
    }

    @Test
    public void testUpdateEntry_UpdatesContent() {
        JournalEntry updated = new JournalEntry();
        updated.setContent("Updated content.");
        updated.setTitle("Updated Title");

        when(journalEntryRepository.findById(1L)).thenReturn(Optional.of(blankEntry));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(i -> i.getArgument(0));

        JournalEntry result = journalEntryService.updateJournalEntry(1L, updated);

        assertEquals("Updated content.", result.getContent());
        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    public void testUpdateEntry_NotFound_ThrowsException() {
        when(journalEntryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                journalEntryService.updateJournalEntry(999L, blankEntry));
    }

    @Test
    public void testDeleteEntry_Success() {
        when(journalEntryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(journalEntryRepository).deleteById(1L);

        journalEntryService.deleteJournalEntry(1L);

        verify(journalEntryRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteEntry_NotFound_ThrowsException() {
        when(journalEntryRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                journalEntryService.deleteJournalEntry(999L));
    }

    @Test
    public void testCountByUserId() {
        when(journalEntryRepository.countByUserId(1L)).thenReturn(5L);

        assertEquals(5L, journalEntryService.countByUserId(1L));
    }
}
