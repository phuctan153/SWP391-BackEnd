package com.example.ev_rental_backend.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException{

    private final HttpStatus status;

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public CustomException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }
}
