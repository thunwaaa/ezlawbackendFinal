# ezlawbackendFinal

This repository contains the backend service for the EZ Law application, built with Spring Boot. It provides a RESTful API for user and lawyer authentication, profile management, and subscription handling via Stripe integration.

## Table of Contents
- [Features](#features)
- [Architecture](#architecture)
- [Technologies Used](#technologies-used)
- [Prerequisites](#prerequisites)
- [Setup and Installation](#setup-and-installation)
- [API Endpoints](#api-endpoints)
- [Configuration](#configuration)

## Features

- **Dual User Types:** Separate authentication and profile management for regular Users and Lawyers.
- **Session-Based Authentication:** Secure user and lawyer login using HTTP sessions.
- **Profile Management:** Endpoints for creating, viewing, and updating user and lawyer profiles.
- **Stripe Integration:** Manages membership subscriptions (weekly, monthly, yearly) using the Stripe payment gateway.
- **Stripe Webhook Handling:** Listens for Stripe events to automatically manage subscription statuses, role changes, and invoice records.
- **Dual Database System:** Utilizes MongoDB for flexible user/lawyer data and MySQL for structured transactional data like invoices and plan details.
- **Role Management:** Automatically assigns and updates user roles (`user`, `Membership`) based on subscription status.

## Architecture

The application is built on a layered architecture typical of Spring Boot applications.

- **Controllers:** Handle incoming HTTP requests, process input, and interact with services. Divided into `Auth` for users and `LawyerAuth` for lawyers.
- **Services:** Contain the core business logic. `UserService`, `LawyerService`, and `StripeService` orchestrate operations like registration, login, profile updates, and payment processing.
- **Repositories:** Data access layer using Spring Data to interact with MongoDB and MySQL.
- **Models:** Define the data structures for users, lawyers, invoices, and plans.
- **Configuration:** Manages CORS, security, session handling, and Stripe API setup.
- **Databases:**
    - **MongoDB:** Stores primary user and lawyer profile data in `User` and `Lawyer` collections.
    - **MySQL:** Stores relational and transactional data, including `users`, `plan_membership`, and `invoice` tables to maintain data integrity for subscriptions.

## Technologies Used

- **Framework:** Spring Boot 3
- **Language:** Java 17
- **Database:**
    - MongoDB (via Spring Data MongoDB)
    - MySQL (via Spring Data JPA/Hibernate)
- **Authentication:** Spring Security with BCrypt for password hashing and session management.
- **Payments:** Stripe Java SDK
- **Build Tool:** Apache Maven

## Prerequisites

- Java Development Kit (JDK) 17 or later
- Apache Maven
- A running MySQL server instance
- A MongoDB instance (local or cloud-based like MongoDB Atlas)
- A Stripe account with API keys

## Setup and Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/thunwaaa/ezlawbackendfinal.git
    cd ezlawbackendfinal/ezlawbackend
    ```

2.  **Configure the application:**
    Open the `src/main/resources/application.properties` file and update the following properties with your specific credentials:

    ```properties
    # MongoDB Connection
    spring.data.mongodb.uri=mongodb+srv://<user>:<password>@<cluster-url>/<database-name>?retryWrites=true&w=majority

    # MySQL Connection
    spring.datasource.url=jdbc:mysql://localhost:3306/law_db
    spring.datasource.username=<your-mysql-username>
    spring.datasource.password=<your-mysql-password>

    # Stripe API Keys
    stripe.apikey=<your-stripe-secret-key>
    stripe.webhook.secret=<your-stripe-webhook-signing-secret>
    ```

3.  **Build and run the application:**
    Use the Maven wrapper to start the server.

    On macOS/Linux:
    ```bash
    ./mvnw spring-boot:run
    ```

    On Windows:
    ```bash
    mvnw.cmd spring-boot:run
    ```

    The application will start on `http://localhost:8080`.

## API Endpoints

The following are the primary API endpoints provided by the service.

### User Authentication (`/api/auth`)

-   `POST /signup`: Registers a new user.
-   `POST /login`: Authenticates a user and creates a session.
-   `POST /logout`: Logs the user out and invalidates the session.
-   `GET /check-session`: Checks if the current session is valid.
-   `PUT /edit-profile`: Updates the logged-in user's profile information.
-   `GET /profile`: Retrieves the profile of the logged-in user.
-   `POST /upgrade`: Initiates a membership subscription process with Stripe.
-   `GET /user_role`: Gets the role of the current user.
-   `GET /user_id`: Gets the ID of the current user.

### Lawyer Authentication (`/api/lawyerauth`)

-   `POST /signup`: Registers a new lawyer.
-   `POST /login`: Authenticates a lawyer and creates a session.
-   `POST /logout`: Logs the lawyer out.
-   `GET /check-session`: Checks the lawyer's session status.
-   `PUT /edit-profile`: Updates the logged-in lawyer's profile.
-   `GET /profile`: Retrieves the profile of the logged-in lawyer.
-   `GET /getlawyer`: Returns a list of all registered lawyers.
-   `GET /getlawyer-by-email/{email}`: Retrieves a lawyer's profile by email.

### General APIs

-   `GET /api/plans`: Retrieves the list of available membership plans.
-   `POST /api/webhook/stripe`: Endpoint for receiving webhook events from Stripe. This should be configured in your Stripe dashboard.

## Configuration

-   **`application.properties`**: The main configuration file for database connections, Stripe keys, and server settings.
-   **`CorsConfig.java`**: Configures Cross-Origin Resource Sharing (CORS) to allow requests from the frontend application (`http://localhost:3000`) and Stripe.
-   **`SessionConfig.java`**: Configures session management, including cookie name and timeout.
-   **`Webconfig.java`**: Sets up interceptors, specifically the `SessionAuthInterceptor` which protects authenticated routes from unauthorized access.
