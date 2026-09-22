package com.example.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AffirmationTest {

    @Test
    void testAffirmationCreation() {
        Affirmation affirmation = new Affirmation();
        affirmation.setAffirmationText("I am capable of amazing things");

        assertNull(affirmation.getId());
        assertEquals("I am capable of amazing things", affirmation.getAffirmationText());
        assertNull(affirmation.getReminders());
    }

    @Test
    void testAffirmationWithAllArgsConstructor() {
        // All 3 parameters: id, affirmationText, reminders
        Affirmation affirmation = new Affirmation(null, "Test affirmation", null);

        assertNull(affirmation.getId());
        assertEquals("Test affirmation", affirmation.getAffirmationText());
        assertNull(affirmation.getReminders());
    }

    @Test
    void testAffirmationNoArgsConstructor() {
        Affirmation affirmation = new Affirmation();
        assertNotNull(affirmation);
        assertNull(affirmation.getId());
        assertNull(affirmation.getAffirmationText());
        assertNull(affirmation.getReminders());
    }

    @Test
    void testAffirmationTextUpdate() {
        Affirmation affirmation = new Affirmation();
        affirmation.setAffirmationText("Original text");
        assertEquals("Original text", affirmation.getAffirmationText());

        affirmation.setAffirmationText("Updated text");
        assertEquals("Updated text", affirmation.getAffirmationText());
    }

    @Test
    void testAffirmationWithReminders() {
        // Just testing that reminders can be set (actual Set would be created in real usage)
        Affirmation affirmation = new Affirmation();
        affirmation.setAffirmationText("Test");
        affirmation.setReminders(null);  // Can be set to a Set later

        assertNull(affirmation.getReminders());
    }
}