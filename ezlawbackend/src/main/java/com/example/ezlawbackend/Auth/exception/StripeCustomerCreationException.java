package com.example.ezlawbackend.Auth.exception;

public class StripeCustomerCreationException extends RuntimeException {
    public StripeCustomerCreationException(String message) {
        super(message);
    }
}
