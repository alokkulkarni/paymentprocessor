# Payment Processor Application - Implementation Summary

## Overview
A comprehensive payment processing system built with Spring Boot 3.5.7 and Java 21, supporting domestic payments and interbank/intrabank transfers with integrated fraud detection and account validation.

## Technology Stack
- **Framework**: Spring Boot 3.5.7
- **Java Version**: 21
- **Data Layer**: Spring Data JDBC (NOT JPA)
- **Database**: PostgreSQL
- **Testing**: JUnit 5, Testcontainers
- **Build Tool**: Maven

## Project Structure

```
src/main/java/com/alok/payment/paymentprocessor/
├── PaymentprocessorApplication.java       # Main application class
├── controller/
│   └── PaymentController.java             # REST endpoints
├── dto/
│   ├── PaymentRequest.java                # Request model
│   ├── PaymentResponse.java               # Response model
│   ├── FraudCheckRequest.java             # Fraud service request
│   ├── FraudCheckResponse.java            # Fraud service response
│   ├── AccountBalanceRequest.java         # Account service request
│   └── AccountBalanceResponse.java        # Account service response
├── model/
│   ├── Payment.java                       # Payment entity (Spring Data JDBC)
│   ├── PaymentType.java                   # Payment type enum
│   └── PaymentStatus.java                 # Payment status enum
├── repository/
│   └── PaymentRepository.java             # Spring Data JDBC repository
└── service/
    ├── PaymentService.java                # Payment orchestration service
    ├── FraudService.java                  # Mock fraud detection service
    └── AccountService.java                # Mock account validation service
```

## Key Components

### 1. Domain Models

#### PaymentType Enum
- `DOMESTIC_PAYMENT` - Domestic payment within the same country
- `DOMESTIC_TRANSFER` - Domestic transfer between accounts
- `INTRABANK_TRANSFER` - Transfer within the same bank
- `INTERBANK_TRANSFER` - Transfer between different banks

#### PaymentStatus Enum
- `PENDING` - Initial state
- `PROCESSING` - Payment in progress
- `COMPLETED` - Successfully processed
- `FRAUD_CHECK_FAILED` - Failed fraud detection
- `INSUFFICIENT_BALANCE` - Insufficient funds
- `ACCOUNT_VALIDATION_FAILED` - Invalid account
- `FAILED` - General failure

#### Payment Entity
Spring Data JDBC entity mapped to `payments` table with fields:
- Transaction ID (UUID)
- From/To accounts
- Amount and currency
- Payment type and status
- Description and failure reason
- Timestamps (created, updated)

### 2. Services

#### PaymentService
Main orchestration service that coordinates:
1. Source account validation
2. Destination account validation
3. Fraud detection check
4. Balance verification
5. Payment execution (debit/credit)
6. Status updates and persistence

Key methods:
- `processPayment(PaymentRequest)` - Process new payment
- `getPaymentStatus(String)` - Get payment by transaction ID
- `getPaymentsByAccount(String)` - Get all payments for account

#### FraudService (Mock)
Simulates fraud detection with:
- Risk scoring based on transaction amount
- High-value transaction flagging (>$50,000)
- Same-account transfer detection
- Random fraud simulation (5% rate for testing)

#### AccountService (Mock)
Simulates account management with:
- Pre-configured mock accounts (ACC001-ACC005)
- Account validation
- Balance checking
- Debit/credit operations
- Random balance generation for unknown accounts

### 3. REST API Endpoints

#### POST /api/payments
Process a new payment request

#### GET /api/payments/{transactionId}
Get status of specific payment

#### GET /api/payments/account/{accountNumber}
Get all payments for an account

#### GET /api/payments
Get all payments (admin/monitoring)

#### GET /api/payments/health
Health check endpoint

### 4. Repository Layer

#### PaymentRepository
Spring Data JDBC repository extending `CrudRepository` with custom queries:
- `findByTransactionId(String)` - Find by transaction ID
- `findByFromAccount(String)` - Find by sender
- `findByToAccount(String)` - Find by receiver
- `findByStatus(PaymentStatus)` - Find by status
- `findByAccount(String)` - Find by either sender or receiver

### 5. Database Schema

PostgreSQL table with:
- BIGSERIAL primary key
- Unique transaction ID index
- Indexes on accounts, status, and created_at
- Decimal(19,2) for monetary amounts
- Timestamp tracking

## Payment Processing Flow

```
1. Receive PaymentRequest
2. Generate unique transaction ID (UUID)
3. Create and save initial Payment entity (PENDING)
4. Validate source account → Fail if invalid
5. Validate destination account → Fail if invalid
6. Perform fraud check → Fail if fraudulent
7. Check source account balance → Fail if insufficient
8. Update status to PROCESSING
9. Deduct from source account
10. Credit to destination account
11. Update status to COMPLETED
12. Return PaymentResponse
```

If any step fails, the payment status is updated accordingly and a failure reason is recorded.

## Mock Data

### Pre-configured Accounts
- ACC001: $100,000.00
- ACC002: $50,000.00
- ACC003: $25,000.00
- ACC004: $5,000.00
- ACC005: $1,000.00

### Fraud Detection Logic
- Transactions >$50,000: 30% fraud probability
- Risk score >0.80: Automatic fraud flag
- Same account transfers: Always flagged
- Normal transactions: 5% random fraud detection

## Configuration

Key configuration in `application.yaml`:
- Database connection (PostgreSQL)
- Connection pooling (HikariCP)
- SQL initialization (schema.sql)
- Jackson JSON settings
- Actuator endpoints
- Logging levels

Environment variables supported:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SERVER_PORT` (default: 8081)

## Testing

Integration test included: `PaymentControllerIntegrationTest`
Tests coverage:
- Health check endpoint
- Successful payment processing
- Insufficient balance scenario
- Payment status retrieval
- Get all payments

## Running the Application

### Prerequisites
1. Java 21
2. Maven
3. PostgreSQL database running on localhost:5432

### Steps
1. Create database: `paymentprocessor`
2. Configure database credentials in application.yaml or environment variables
3. Run: `./mvnw spring-boot:run`
4. Access: `http://localhost:8081`

### Testing with cURL

```bash
# Process a payment
curl -X POST http://localhost:8081/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC001",
    "toAccount": "ACC002",
    "amount": 1000.00,
    "currency": "USD",
    "paymentType": "DOMESTIC_PAYMENT",
    "description": "Test payment"
  }'

# Get payment status
curl http://localhost:8081/api/payments/{transactionId}

# Get account payments
curl http://localhost:8081/api/payments/account/ACC001
```

## Features Implemented

✅ Payment request processing with validation
✅ Mock fraud detection service with risk scoring
✅ Mock account service with balance validation
✅ Spring Data JDBC persistence (no JPA/Lombok)
✅ Transaction ID generation (UUID)
✅ Comprehensive payment status tracking
✅ RESTful API endpoints
✅ Database schema with indexes
✅ Detailed logging
✅ Error handling and failure reasons
✅ Support for multiple payment types
✅ Intrabank and interbank payment support
✅ Integration tests

## Code Characteristics

- **No Lombok**: Uses standard Java getters/setters
- **Spring Data JDBC**: Simple JDBC-based persistence, not JPA
- **Constructor-based DI**: Services use constructor injection
- **Clean separation**: Controller → Service → Repository
- **Immutable operations**: Transaction-based payment processing
- **Comprehensive logging**: SLF4J with contextual information

## Future Enhancements

Potential improvements:
- Async payment processing
- Payment cancellation/reversal
- Multi-currency support with exchange rates
- Real fraud detection API integration
- Real account service API integration
- Payment scheduling
- Batch payment processing
- Payment notifications
- Audit logging
- API authentication/authorization

## Documentation

- `API_DOCUMENTATION.md` - Complete API reference with examples
- `IMPLEMENTATION_SUMMARY.md` - This file
- Inline code comments for complex logic
