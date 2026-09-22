package com.example.model;

import com.example.model.enumerations.ReminderType;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class ReminderTest {

    @Test
    public void testReminderCreation() {
        User user = new User();
        user.setId(1L);

        Reminder reminder = new Reminder();
        reminder.setUser(user);
        reminder.setTimeOfDay(LocalTime.of(9, 0));
        reminder.setDaysOfWeek("MON,TUE,WED");
        reminder.setType(ReminderType.GENERAL);
        reminder.setMessage("Morning check-in");
        reminder.setEnabled(true);

        assertEquals(LocalTime.of(9, 0), reminder.getTimeOfDay());
        assertEquals("MON,TUE,WED", reminder.getDaysOfWeek());
        assertEquals(ReminderType.GENERAL, reminder.getType());
        assertEquals("Morning check-in", reminder.getMessage());
        assertTrue(reminder.isEnabled());
        assertEquals(user, reminder.getUser());
    }

    @Test
    public void testReminderDefaultsToNoLinkedAffirmation() {
        Reminder reminder = new Reminder();
        assertNull(reminder.getLinkedAffirmation());
    }

    @Test
    public void testReminderToggleEnabled() {
        Reminder reminder = new Reminder();
        reminder.setEnabled(true);
        assertTrue(reminder.isEnabled());

        reminder.setEnabled(false);
        assertFalse(reminder.isEnabled());
    }

    @Test
    public void testAffirmationReminderLinksAffirmation() {
        Affirmation affirmation = new Affirmation();
        affirmation.setId(1L);
        affirmation.setAffirmationText("I am enough.");

        Reminder reminder = new Reminder();
        reminder.setType(ReminderType.AFFIRMATION);
        reminder.setLinkedAffirmation(affirmation);
        reminder.setMessage(affirmation.getAffirmationText());

        assertEquals(ReminderType.AFFIRMATION, reminder.getType());
        assertEquals("I am enough.", reminder.getMessage());
        assertEquals(affirmation, reminder.getLinkedAffirmation());
    }

    @Test
    public void testReminderTypeValues() {
        assertNotNull(ReminderType.AFFIRMATION);
        assertNotNull(ReminderType.GENERAL);
        assertEquals(2, ReminderType.values().length);
    }
}