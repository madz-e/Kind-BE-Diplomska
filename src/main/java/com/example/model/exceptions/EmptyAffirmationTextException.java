package com.example.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmptyAffirmationTextException extends RuntimeException {

    public EmptyAffirmationTextException() {
        super("Affirmation text cannot be null or empty");
    }

    public EmptyAffirmationTextException(String text) {
        super(String.format("Invalid affirmation text: '%s'. Affirmation text cannot be null or empty.", text));
    }
}