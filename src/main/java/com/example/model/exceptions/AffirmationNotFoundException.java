package com.example.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AffirmationNotFoundException extends RuntimeException {
    public AffirmationNotFoundException(Long id) {
        super(String.format("Affirmation with id: %d was not found", id));
    }
}
