package com.example.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ReminderNotFoundException extends RuntimeException {
    public ReminderNotFoundException(Long id) {
        super(String.format("Reminder with id: %d was not found", id));
    }
}
