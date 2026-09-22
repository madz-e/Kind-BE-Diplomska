package com.example.service.impl;

import com.example.jpaRepository.ReminderRepository;
import com.example.model.Affirmation;
import com.example.model.Reminder;
import com.example.model.User;
import com.example.model.enumerations.ReminderType;
import com.example.model.exceptions.ReminderNotFoundException;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserService userService;
    private final AffirmationService affirmationService;

    /**
     * 1. CREATE - Create a new reminder
     */
    @Transactional
    public Reminder createReminder(Reminder reminder) {
        // Validate and get user using UserService
        User user = userService.findById(reminder.getUser().getId()).orElseThrow(); //todo custom exception?
        reminder.setUser(user);

        // If it's an affirmation reminder, validate affirmation
        if (reminder.getType() == ReminderType.AFFIRMATION &&
                reminder.getLinkedAffirmation() != null) {
            Affirmation affirmation = affirmationService.getAffirmationById(
                    reminder.getLinkedAffirmation().getId()
            );
            reminder.setLinkedAffirmation(affirmation);

            // Use affirmation text as message
            if (reminder.getMessage() == null) {
                reminder.setMessage(affirmation.getAffirmationText());
            }
        }

        // Validate days of week
        validateDaysOfWeek(reminder.getDaysOfWeek());

        return reminderRepository.save(reminder);
    }

    /**
     * 2. READ - Get reminder by ID
     */
    @Transactional(readOnly = true)
    public Reminder getReminderById(Long id) {
        return reminderRepository.findById(id)
                .orElseThrow(() -> new ReminderNotFoundException(id));
    }

    /**
     * 3. READ - Get all reminders for a user
     */
    @Transactional(readOnly = true)
    public List<Reminder> getUserReminders(Long userId) {
        // Validate user exists using UserService
        userService.findById(userId);

        return reminderRepository.findByUserIdOrderByTimeOfDayAsc(userId);
    }

    /**
     * 4. READ - Get all reminders
     */
    @Transactional(readOnly = true)
    public List<Reminder> getAllReminders() {
        return reminderRepository.findAll();
    }

    /**
     * 5. UPDATE - Update existing reminder
     */
    @Transactional
    public Reminder updateReminder(Long id, Reminder updatedReminder) {
        Reminder existingReminder = getReminderById(id);

        // Only update allowed fields
        existingReminder.setTimeOfDay(updatedReminder.getTimeOfDay());
        existingReminder.setDaysOfWeek(updatedReminder.getDaysOfWeek());
        existingReminder.setEnabled(updatedReminder.isEnabled());
        existingReminder.setMessage(updatedReminder.getMessage());

        // Validate days
        validateDaysOfWeek(existingReminder.getDaysOfWeek());

        return reminderRepository.save(existingReminder);
    }

    /**
     * 6. UPDATE - Toggle reminder enabled/disabled
     */
    @Transactional
    public Reminder toggleReminder(Long id, boolean enabled) {
        Reminder reminder = getReminderById(id);
        reminder.setEnabled(enabled);
        return reminderRepository.save(reminder);
    }

    /**
     * 7. DELETE - Delete reminder
     */
    @Transactional
    public void deleteReminder(Long id) {
        Reminder reminder = getReminderById(id);
        reminderRepository.delete(reminder);
    }

    /**
     * Create an affirmation reminder (convenience method)
     */
    @Transactional
    public Reminder createAffirmationReminder(Long userId, Long affirmationId,
                                              LocalTime timeOfDay, String daysOfWeek) {
        // Get user and affirmation through services
        User user = userService.findById(userId).orElseThrow(); //todo custom exception?
        Affirmation affirmation = affirmationService.getAffirmationById(affirmationId);

        // Create reminder
        Reminder reminder = new Reminder();
        reminder.setUser(user);
        reminder.setTimeOfDay(timeOfDay);
        reminder.setDaysOfWeek(daysOfWeek);
        reminder.setType(ReminderType.AFFIRMATION);
        reminder.setMessage(affirmation.getAffirmationText());
        reminder.setEnabled(true);
        reminder.setLinkedAffirmation(affirmation);

        return createReminder(reminder);
    }

    /**
     * Create a general reminder (convenience method)
     */
    @Transactional
    public Reminder createGeneralReminder(Long userId, LocalTime timeOfDay,
                                          String daysOfWeek, String message) {
        User user = userService.findById(userId).orElseThrow();//todo custom exception?

        Reminder reminder = new Reminder();
        reminder.setUser(user);
        reminder.setTimeOfDay(timeOfDay);
        reminder.setDaysOfWeek(daysOfWeek);
        reminder.setType(ReminderType.GENERAL);
        reminder.setMessage(message);
        reminder.setEnabled(true);
        reminder.setLinkedAffirmation(null);

        return createReminder(reminder);
    }

    /**
     * Get user's affirmation reminders
     */
    @Transactional(readOnly = true)
    public List<Reminder> getUserAffirmationReminders(Long userId) {
        userService.findById(userId); // Validate user exists

        return reminderRepository.findByUserIdOrderByTimeOfDayAsc(userId).stream()
                .filter(r -> r.getType() == ReminderType.AFFIRMATION)
                .toList();
    }

    /**
     * Get user's general reminders
     */
    @Transactional(readOnly = true)
    public List<Reminder> getUserGeneralReminders(Long userId) {
        userService.findById(userId); // Validate user exists

        return reminderRepository.findByUserIdOrderByTimeOfDayAsc(userId).stream()
                .filter(r -> r.getType() == ReminderType.GENERAL)
                .toList();
    }

    //helper
    private void validateDaysOfWeek(String daysOfWeek) {
        if (daysOfWeek == null || daysOfWeek.trim().isEmpty()) {
            throw new IllegalArgumentException("Days of week cannot be empty");
        }

        // Basic validation - check format
        String[] days = daysOfWeek.split(",");
        if (days.length == 0) {
            throw new IllegalArgumentException("Invalid days format. Use: MON,TUE,WED");
        }

    }


}