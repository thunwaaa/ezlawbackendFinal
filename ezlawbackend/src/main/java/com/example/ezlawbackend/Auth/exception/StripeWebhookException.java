package com.example.ezlawbackend.Auth.exception;

public class StripeWebhookException extends RuntimeException {
    public StripeWebhookException(String message) {
        super(message);
    }
}
