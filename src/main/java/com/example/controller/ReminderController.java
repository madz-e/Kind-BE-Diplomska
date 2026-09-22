package com.example.controller;

import com.example.model.Reminder;
import com.example.model.enumerations.ReminderType;
import com.example.model.exceptions.ReminderNotFoundException;
import com.example.service.impl.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // For mobile app development
public class ReminderController {

    private final ReminderService reminderService;

    /**
     * GET /api/reminders/user/{userId}
     * Get all reminders for a specific user
     * Sorted by time of day (ascending)
     * Response: List of Reminder objects
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Reminder>> getUserReminders(@PathVariable Long userId) {
        return ResponseEntity.ok(reminderService.getUserReminders(userId));
    }

    /**
     * GET /api/reminders
     * Get all reminders (admin/debug endpoint)
     */
    @GetMapping
    public ResponseEntity<List<Reminder>> getAllReminders() {
        return ResponseEntity.ok(reminderService.getAllReminders());
    }

    /**
     * GET /api/reminders/{id}
     * Get specific reminder by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getReminderById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(reminderService.getReminderById(id));
        } catch (ReminderNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * GET /api/reminders/user/{userId}/affirmations
     * Get all affirmation reminders for a user
     */
    @GetMapping("/user/{userId}/affirmations")
    public ResponseEntity<List<Reminder>> getUserAffirmationReminders(@PathVariable Long userId) {
        return ResponseEntity.ok(reminderService.getUserAffirmationReminders(userId));
    }

    /**
     * GET /api/reminders/user/{userId}/general
     * Get all general reminders for a user
     */
    @GetMapping("/user/{userId}/general")
    public ResponseEntity<List<Reminder>> getUserGeneralReminders(@PathVariable Long userId) {
        return ResponseEntity.ok(reminderService.getUserGeneralReminders(userId));
    }

    /**
     * POST /api/reminders
     * Create a new reminder
     * Request Body:
     * {
     * "user": { "id": 1 },
     * "timeOfDay": "09:00:00",
     * "daysOfWeek": "MON,TUE,WED,THU,FRI",
     * "type": "GENERAL",
     * "message": "Time for your morning meditation",
     * "enabled": true,
     * "linkedAffirmation": null // or { "id": 3 } for AFFIRMATION type
     * }
     * <p>
     * Available types: AFFIRMATION, MEDITATION, HABIT, JOURNAL, GENERAL
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReminder(@RequestBody Reminder reminder) {
        try {
            Reminder created = reminderService.createReminder(reminder);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reminder created successfully");
            response.put("reminder", created);
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * POST /api/reminders/affirmation
     * Create an affirmation reminder (convenience endpoint)
     * Request Body:
     * {
     * "userId": 1,
     * "affirmationId": 3,
     * "timeOfDay": "09:00:00",
     * "daysOfWeek": "MON,TUE,WED,THU,FRI"
     * }
     */
    @PostMapping("/affirmation")
    public ResponseEntity<Map<String, Object>> createAffirmationReminder(
            @RequestBody Map<String, Object> request) {

        try {
            Long userId = ((Number) request.get("userId")).longValue();
            Long affirmationId = ((Number) request.get("affirmationId")).longValue();
            LocalTime timeOfDay = LocalTime.parse((String) request.get("timeOfDay"));
            String daysOfWeek = (String) request.get("daysOfWeek");

            Reminder created = reminderService.createAffirmationReminder(
                    userId, affirmationId, timeOfDay, daysOfWeek
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Affirmation reminder created successfully");
            response.put("reminder", created);
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * POST /api/reminders/general
     * Create a general reminder (convenience endpoint)
     * Request Body:
     * {
     * "userId": 1,
     * "timeOfDay": "14:00:00",
     * "daysOfWeek": "MON,WED,FRI",
     * "message": "Time to journal your thoughts"
     * }
     */
    @PostMapping("/general")
    public ResponseEntity<Map<String, Object>> createGeneralReminder(
            @RequestBody Map<String, Object> request) {

        try {
            Long userId = ((Number) request.get("userId")).longValue();
            LocalTime timeOfDay = LocalTime.parse((String) request.get("timeOfDay"));
            String daysOfWeek = (String) request.get("daysOfWeek");
            String message = (String) request.get("message");

            Reminder created = reminderService.createGeneralReminder(
                    userId, timeOfDay, daysOfWeek, message
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "General reminder created successfully");
            response.put("reminder", created);
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * PUT /api/reminders/{id}
     * Update an existing reminder
     * Request Body: (all fields optional except those you want to update)
     * {
     * "timeOfDay": "10:00:00",
     * "daysOfWeek": "MON,TUE,WED",
     * "enabled": false,
     * "message": "Updated message"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<Reminder> updateReminder(
            @PathVariable Long id,
            @RequestBody Reminder updatedReminder) {

        Reminder updated = reminderService.updateReminder(id, updatedReminder);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH /api/reminders/{id}/toggle
     * Toggle reminder enabled/disabled
     * Request param: enabled (boolean)
     * Example: PATCH /api/reminders/1/toggle?enabled=false
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleReminder(
            @PathVariable Long id,
            @RequestParam boolean enabled) {

        Reminder updated = reminderService.toggleReminder(id, enabled);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Reminder " + (enabled ? "enabled" : "disabled"));
        response.put("reminder", updated);
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/reminders/{id}
     * Delete a reminder
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReminder(@PathVariable Long id) {
        try {
            reminderService.deleteReminder(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reminder deleted successfully");
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * GET /api/reminders/types
     * Get all available reminder types
     * Response:
     * {
     * "types": ["AFFIRMATION", "MEDITATION", "HABIT", "JOURNAL", "GENERAL"],
     * "timestamp": "2024-01-15T14:30:00"
     * }
     */
    @GetMapping("/types")
    public ResponseEntity<Map<String, Object>> getReminderTypes() {
        Map<String, Object> response = new HashMap<>();
        response.put("types", ReminderType.values());
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}