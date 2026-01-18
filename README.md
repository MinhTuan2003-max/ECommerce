# E-Commerce Backend System (Spring Boot)

This project is a **production-oriented backend system** for a fashion e-commerce platform, designed with a strong focus on **correctness under concurrency**, **guest checkout**, and **high-load scenarios** such as flash sales.

The system supports **Guest Checkout**, integrates **SePay (VietQR) payment**, and implements **pessimistic locking** at the database level to prevent **overselling** when multiple customers attempt to purchase the same product simultaneously.

---

## Key Objectives

* Support fast checkout without requiring user registration (Guest Checkout)
* Ensure inventory consistency under concurrent access
* Integrate bank transfer payments via SePay (VietQR)
* Provide clear API documentation and stress-testing capability
* Be easy to run locally for development and evaluation

---

## Technology Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.x
* **Build Tool:** Maven (Maven Wrapper included)
* **Database:** PostgreSQL
* **ORM:** Hibernate / Spring Data JPA
* **Search Engine:** Elasticsearch 7.17.10
* **Security:** Spring Security, JWT (Admin/User), Public access for Guest
* **Payment Integration:** SePay (VietQR) with Webhook handling
* **API Documentation:** Swagger UI (OpenAPI 3)
* **Load & Stress Testing:** Grafana k6

---

## Core Features

### 1. Order & Checkout Flow

* **Guest Checkout**

  * Customers can place orders and pay without creating an account.
  * Orders are associated with a temporary session identifier.

* **Flexible Authorization**

  * Supports both authenticated users and guest users.
  * Public endpoints for checkout, secured endpoints for admin operations.

* **SePay Payment Integration**

  * Dynamically generates VietQR payment requests based on order amount.
  * Handles SePay Webhooks to automatically update order status.
  * Secure webhook signature verification.
  * Demo / simulation mode for local testing without real bank transfers.

---

### 2. Inventory Management & Concurrency Control

* **Pessimistic Locking**

  * Uses database-level row locking (`SELECT ... FOR UPDATE`) to guarantee inventory consistency.
  * Prevents overselling even under heavy concurrent requests.

* **Deadlock Prevention**

  * Inventory resources are locked in a deterministic order to avoid deadlocks
    when multiple product variants are purchased in the same order.

* **Inventory Reservation**

  * Stock is reserved during checkout.
  * Reservations can be released or finalized based on payment outcome.
  * Expired reservations are cleaned up automatically.

---

### 3. Product Search (Elasticsearch)

* **Elasticsearch 7.17.10 Integration**

  * Dedicated search index for products and variants.
  * Supports keyword-based product search with high performance.
  * Designed to offload search queries from the relational database.

* **Separation of Concerns**

  * PostgreSQL is used for transactional data.
  * Elasticsearch is used for search and filtering.

---

### 4. Shopping Cart

* Session-based cart for Guest users.
* Database-backed cart for authenticated users.
* APIs for adding, updating, removing, and synchronizing cart items.

---

## Project Requirements

Before running the project, ensure the following are installed:

1. **Java JDK 21**
2. **Maven** (or use the included Maven Wrapper)
3. **PostgreSQL**
4. **Elasticsearch 7.17.10**
5. **k6** (optional, for stress testing)

---

## Configuration

Create an `application.properties` (or `application.yml`) file under `src/main/resources`.

### Application Configuration Example

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce_db
spring.datasource.username=postgres
spring.datasource.password=your_password

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Elasticsearch
spring.elasticsearch.uris=http://localhost:9200
spring.elasticsearch.username=
spring.elasticsearch.password=

# SePay Payment
# Used to verify webhook signatures from SePay
app.payment.sepay.webhook-key=your_sepay_webhook_key

# Frontend URL (used for redirects or demo payment page)
app.frontend.url=http://localhost:8080/payment-demo

# JWT (for authenticated users / admin)
application.security.jwt.secret-key=your_very_long_secret_key
application.security.jwt.expiration=86400000
```

---

## Running the Project

### 1. Start PostgreSQL

Ensure PostgreSQL is running and a database named `ecommerce_db` exists.

### 2. Start Elasticsearch

Run Elasticsearch **version 7.17.10** locally (example for Windows):

```bash
bin/elasticsearch.bat
```

Verify it is running at:

```
http://localhost:9200
```

---

### 3. Run the Application

From the project root directory:

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
.\mvnw.cmd spring-boot:run
```

---

### 4. Access the Application

* API Base URL: `http://localhost:8080`
* Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

## Payment Demo (SePay)

To test the payment flow without a frontend application:

1. Create an order via `POST /api/v1/orders`
2. Copy the returned `orderId`
3. Open in browser:

   ```
   http://localhost:8080/api/v1/payments/{orderId}/initiate?method=SEPAY
   ```
4. Use the demo page to simulate a successful payment.
5. The webhook flow will update the order status automatically.

---

## Stress Testing with k6

The project includes a `k6` directory containing load test scripts.

### Inventory Race Condition Test

This test simulates multiple users attempting to purchase the same product
with limited stock.

**Steps:**

1. Install k6.
2. Open `k6/race_condition_test.js`.
3. Set `VARIANT_ID` to an existing product variant UUID.
4. Set inventory quantity to `10` in the database.
5. Run:

```bash
cd k6
k6 run race_condition_test.js
```

**Expected Result:**

* 10 requests succeed (`201 Created`)
* Remaining requests fail with `400 Bad Request` (out of stock)
* No `500 Internal Server Error`

---

## Project Structure

```
src/main/java/fpt/tuanhm43/server
├── controllers     # REST API endpoints
├── services        # Business logic
├── repositories    # Spring Data JPA repositories
├── entities        # Database entities
├── dtos            # Request / Response DTOs
├── config          # Security, Swagger, application configuration
├── exceptions      # Centralized exception handling
└── enums           # Domain enums

k6/
└── *.js            # Load and stress test scripts
```

---

## Contribution

This project is developed and maintained by **TuanHM**.

Contributions are welcome via Pull Requests. Please ensure code quality
and consistency with existing architectural decisions.
