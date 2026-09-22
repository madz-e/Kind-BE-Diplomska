package com.example.model;

import com.example.model.enumerations.EntryType;
import com.example.model.enumerations.JournalPromptType;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class JournalEntryTest {

    @Test
    public void testBlankJournalEntry() {
        User user = new User();
        user.setId(1L);

        JournalEntry entry = new JournalEntry();
        entry.setUser(user);
        entry.setTitle("My Day");
        entry.setContent("Today was a good day.");
        entry.setType(EntryType.BLANK);
        entry.setCreatedAt(LocalDate.now());

        assertEquals("My Day", entry.getTitle());
        assertEquals(EntryType.BLANK, entry.getType());
        assertEquals(LocalDate.now(), entry.getCreatedAt());
        assertNull(entry.getJournalPrompt());
    }

    @Test
    public void testPromptBasedJournalEntry() {
        JournalPrompt prompt = new JournalPrompt();
        prompt.setId(1L);
        prompt.setPromptText("What are you grateful for?");
        prompt.setType(JournalPromptType.GENERAL);

        JournalEntry entry = new JournalEntry();
        entry.setType(EntryType.PROMPT_BASED);
        entry.setContent("I am grateful for my family.");
        entry.setJournalPrompt(prompt);

        assertEquals(EntryType.PROMPT_BASED, entry.getType());
        assertNotNull(entry.getJournalPrompt());
        assertEquals("What are you grateful for?", entry.getJournalPrompt().getPromptText());
    }

    @Test
    public void testJournalPromptGetQuestions_AntExercise() {
        JournalPrompt prompt = new JournalPrompt();
        prompt.setType(JournalPromptType.ANT_EXERCISE);
        prompt.setPromptText("ANT Exercise");

        String[] questions = prompt.getQuestions();

        assertEquals(7, questions.length);
        assertEquals("What happened?", questions[0]);
        assertEquals("How do I feel now?", questions[6]);
    }

    @Test
    public void testJournalPromptGetQuestions_General_ReturnsPromptText() {
        JournalPrompt prompt = new JournalPrompt();
        prompt.setType(JournalPromptType.GENERAL);
        prompt.setPromptText("What are you grateful for today?");

        String[] questions = prompt.getQuestions();

        assertEquals(1, questions.length);
        assertEquals("What are you grateful for today?", questions[0]);
    }
}
