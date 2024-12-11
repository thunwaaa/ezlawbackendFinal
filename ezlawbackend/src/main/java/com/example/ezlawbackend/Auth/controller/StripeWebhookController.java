package com.example.ezlawbackend.Auth.controller;

import com.example.ezlawbackend.Auth.service.StripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook")
public class StripeWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    private final StripeService stripeService;

    @Value("${stripe.webhook.secret}") // Fetch the secret from application properties or environment
    private String endpointSecret;

    public StripeWebhookController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/stripe")
    public ResponseEntity<String> handleWebhook(
            @RequestBody(required = false) String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader,
            HttpServletRequest request
    ) {
        logger.info("Webhook received from IP: {}", request.getRemoteAddr());

        // Validate required fields
        if (payload == null || sigHeader == null) {
            logger.error("Missing payload or Stripe-Signature header");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing payload or Stripe-Signature header");
        }

        logger.info("Stripe-Signature Header: {}", sigHeader);
        logger.info("Endpoint Secret Length: {}", endpointSecret.length());

        try {
            // Verify the Stripe webhook signature
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            logger.info("Webhook verified: Event type: {}", event.getType());

            // Process the event
            stripeService.handleEvent(event);
            logger.info("Webhook processed successfully for event type: {}", event.getType());
            return ResponseEntity.ok("Webhook processed successfully");

        } catch (SignatureVerificationException e) {
            logger.error("Signature verification failed", e);
            logger.error("Payload: {}", payload);
            logger.error("Signature Header: {}", sigHeader);
            logger.error("Endpoint Secret Length: {}", endpointSecret.length());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Signature");

        } catch (Exception e) {
            logger.error("Unexpected error while processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook processing failed");
        }
    }
}
