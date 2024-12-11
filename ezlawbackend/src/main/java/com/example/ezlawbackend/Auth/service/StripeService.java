package com.example.ezlawbackend.Auth.service;

import com.example.ezlawbackend.Auth.model.UserMySQL;
import com.example.ezlawbackend.Auth.repository.UserMySQLRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

    @Autowired
    private UserMySQLRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private InvoiceService invoiceService;

    @Value("${stripe.apikey}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public Customer createStripeCustomer(UserMySQL user) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("email", user.getEmail());
            params.put("name", user.getFirstname() + " " + user.getLastname());
            params.put("preferred_locales", new String[]{"th"}); // Thai locale

            Customer customer = Customer.create(params);
            user.setStripeCustomerId(customer.getId());
            userRepository.save(user);

            return customer;
        } catch (StripeException e) {
            logger.error("Error creating Stripe customer", e);
            throw new RuntimeException("Failed to create Stripe customer", e);
        }
    }

    public Subscription createThaiSubscription(UserMySQL user, String planType) {
        try {
            // Ensure customer exists
            if (user.getStripeCustomerId() == null) {
                createStripeCustomer(user);
            }

            Map<String, Object> params = new HashMap<>();
            params.put("customer", user.getStripeCustomerId());
            params.put("items", Arrays.asList(
                    Map.of("price", getThbPriceId(planType))
            ));
            params.put("payment_behavior", "default_incomplete");
            params.put("expand", Arrays.asList("latest_invoice"));

            Subscription subscription = Subscription.create(params);

            String priceId = subscription.getItems().getData().get(0).getPrice().getId();
            long amount = subscription.getItems().getData().get(0).getPrice().getUnitAmount();

            // Create invoice record
            invoiceService.createThaiInvoice(
                    user.getEmail(),
                    planType,
                    amount / 100.0,
                    subscription.getId()
            );

            return subscription;
        } catch (StripeException e) {
            logger.error("Subscription creation failed", e);
            throw new RuntimeException("Subscription creation failed", e);
        }
    }

    public void handleCheckoutSessionCompleted(Session session) {
        try {
            String email = session.getCustomerEmail();
            UserMySQL user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update user to member
            user.setRole("Membership");
            user.setMember(true);
            userRepository.save(user);

            String planType = session.getMetadata().get("plan_type"); // Plan type from metadata
            double amountPaid = session.getAmountTotal() / 100.0; // Amount in THB
            String subscriptionId = session.getSubscription();

            logger.info("Checkout session completed for user: {}", email);
        } catch (Exception e) {
            logger.error("Error processing checkout session", e);
        }
    }

    public void handleInvoicePaymentFailed(String email) {
        try {
            UserMySQL user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Downgrade user to free tier
            user.setRole("user");
            user.setMember(false);
            userRepository.save(user);

            logger.info("User downgraded due to failed payment: {}", email);
        } catch (Exception e) {
            logger.error("Error processing payment failure", e);
        }
    }

    public void handleSubscriptionDeleted(Subscription subscription) {
        try {
            String customerId = subscription.getCustomer();
            Customer customer = Customer.retrieve(customerId); // ดึงข้อมูล Customer
            String customerEmail = customer.getEmail(); // อีเมลของลูกค้า
            String subscriptionId = subscription.getId();

            userService.handleSubscriptionEvent(customerEmail, subscriptionId, "cancelled", null, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to handle subscription deletion", e);
        }
    }

    public void handleEvent(Event event) {
        try {
            switch (event.getType()) {

                case "checkout.session.completed":
                    Session session = (Session) event.getData().getObject();
                    handleCheckoutSessionCompleted(session);
                    break;

                case "customer.subscription.created":
                    break;
                case "customer.subscription.updated":
                    Subscription subscription = (Subscription) event.getData().getObject();
                    String email = retrieveCustomerEmail(subscription);
                    String subscriptionId = subscription.getId();
                    String status = subscription.getStatus();
                    LocalDateTime startDate = LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(subscription.getCurrentPeriodStart()),
                            ZoneId.systemDefault()
                    );
                    LocalDateTime endDate = LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(subscription.getCurrentPeriodEnd()),
                            ZoneId.systemDefault()
                    );
                    handleSubscriptionEvent(email, subscriptionId, status, startDate, endDate);
                    break;

                case "customer.subscription.deleted":
                    Subscription deletedSubscription = (Subscription) event.getData().getObject();
                    handleSubscriptionDeleted(deletedSubscription);
                    break;

                default:
                    logger.info("Unhandled event type: {}", event.getType());
            }
        } catch (Exception e) {
            logger.error("Error handling event: {}", event.getType(), e);
        }
    }


    private LocalDateTime calculateSubscriptionEnd(String duration, LocalDateTime start) {
        switch (duration.toLowerCase()) {
            case "weekly": return start.plusWeeks(1);
            case "monthly": return start.plusMonths(1);
            case "yearly": return start.plusYears(1);
            default: throw new IllegalArgumentException("Invalid duration");
        }
    }

    private String retrieveCustomerEmail(Subscription subscription) throws StripeException {
        com.stripe.model.Customer customer =
                com.stripe.model.Customer.retrieve(subscription.getCustomer());

        return customer.getEmail();
    }

    public void handleSubscriptionEvent(String email, String subscriptionId, String status, LocalDateTime startDate, LocalDateTime endDate) {
        userService.handleSubscriptionEvent(email, subscriptionId, status, startDate, endDate);
    }


    private String getThbPriceId(String planType) {
        // Map to your actual Stripe price IDs for Thai market
        switch(planType) {
            case "weekly": return "price_thai_weekly";
            case "monthly": return "price_thai_monthly";
            case "yearly": return "price_thai_yearly";
            default: throw new IllegalArgumentException("Invalid plan type");
        }
    }

    public Event constructEvent(String payload, String sigHeader) {
        try {
            logger.info("Payload: {}", payload);
            logger.info("Signature Header: {}", sigHeader);
            logger.info("Webhook Secret: {}", stripeWebhookSecret);
            return Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (Exception e) {
            logger.error("Webhook verification failed", e);
            throw new RuntimeException("Webhook verification failed", e);
        }
    }

    private int determinePlanDuration(String planType) {
        switch(planType) {
            case "weekly": return 1;
            case "monthly": return 1;
            case "yearly": return 12;
            default: throw new IllegalArgumentException("Invalid plan type");
        }
    }
}