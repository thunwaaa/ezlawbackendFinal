package com.example.ezlawbackend.config;

import com.example.ezlawbackend.Auth.service.StripeService;
import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

    @Value("${stripe.apikey}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
        logger.info("Stripe Service initialized with API Key");
    }
}
