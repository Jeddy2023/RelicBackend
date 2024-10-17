package com.example.relicv2.exception;

import org.springframework.http.HttpStatus;

public class RelicException extends RuntimeException {
    private HttpStatus status;
    private String message;

    public RelicException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public RelicException(String message, HttpStatus status, String message1) {
        super(message);
        this.status = status;
        this.message = message1;
    }

    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
