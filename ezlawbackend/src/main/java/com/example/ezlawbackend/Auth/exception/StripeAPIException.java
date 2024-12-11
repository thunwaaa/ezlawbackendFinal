package com.example.ezlawbackend.Auth.exception;

public class StripeAPIException extends RuntimeException {
    public StripeAPIException(String message) {
        super(message);
    }

    public StripeAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
