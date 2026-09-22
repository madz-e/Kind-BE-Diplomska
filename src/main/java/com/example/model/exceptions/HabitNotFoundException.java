package com.example.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class HabitNotFoundException extends RuntimeException {
    public HabitNotFoundException(Long id) {
        super(String.format("Habit with id: %d was not found", id));
    }
}