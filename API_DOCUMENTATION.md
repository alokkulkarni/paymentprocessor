# Payment Processor API Documentation

## Overview
The Payment Processor application handles domestic payments and interbank/intrabank transfers. It validates payments through mock fraud detection and account balance verification services.

## Base URL
```
http://localhost:8081/api/payments
```

## Endpoints

### 1. Process Payment
Creates and processes a new payment transaction.

**Endpoint:** `POST /api/payments`

**Request Body:**
```json
{
  "fromAccount": "ACC001",
  "toAccount": "ACC002",
  "amount": 1000.00,
  "currency": "USD",
  "paymentType": "DOMESTIC_PAYMENT",
  "description": "Payment for services"
}
```

**Payment Types:**
- `DOMESTIC_PAYMENT` - Domestic payment within the same country
- `DOMESTIC_TRANSFER` - Domestic transfer between accounts
- `INTRABANK_TRANSFER` - Transfer within the same bank
- `INTERBANK_TRANSFER` - Transfer between different banks

**Success Response (200 OK):**
```json
{
  "transactionId": "123e4567-e89b-12d3-a456-426614174000",
  "fromAccount": "ACC001",
  "toAccount": "ACC002",
  "amount": 1000.00,
  "currency": "USD",
  "paymentType": "DOMESTIC_PAYMENT",
  "status": "COMPLETED",
  "message": "Payment successful",
  "failureReason": null,
  "timestamp": "2025-11-14T10:30:00"
}
```

**Failure Response (400 Bad Request):**
```json
{
  "transactionId": "123e4567-e89b-12d3-a456-426614174000",
  "fromAccount": "ACC001",
  "toAccount": "ACC002",
  "amount": 1000.00,
  "currency": "USD",
  "paymentType": "DOMESTIC_PAYMENT",
  "status": "INSUFFICIENT_BALANCE",
  "message": "Payment unsuccessful",
  "failureReason": "Insufficient balance. Available: 500.00, Required: 1000.00",
  "timestamp": "2025-11-14T10:30:00"
}
```

**Payment Statuses:**
- `PENDING` - Payment initiated
- `PROCESSING` - Payment being processed
- `COMPLETED` - Payment successful
- `FRAUD_CHECK_FAILED` - Failed fraud detection
- `INSUFFICIENT_BALANCE` - Insufficient funds
- `ACCOUNT_VALIDATION_FAILED` - Invalid account
- `FAILED` - General failure

### 2. Get Payment Status
Retrieves the status of a specific payment by transaction ID.

**Endpoint:** `GET /api/payments/{transactionId}`

**Example:**
```
GET /api/payments/123e4567-e89b-12d3-a456-426614174000
```

**Success Response (200 OK):**
```json
{
  "transactionId": "123e4567-e89b-12d3-a456-426614174000",
  "fromAccount": "ACC001",
  "toAccount": "ACC002",
  "amount": 1000.00,
  "currency": "USD",
  "paymentType": "DOMESTIC_PAYMENT",
  "status": "COMPLETED",
  "message": "Payment successful",
  "timestamp": "2025-11-14T10:30:00"
}
```

**Not Found Response (404):**
Returns 404 if transaction ID doesn't exist.

### 3. Get Payments by Account
Retrieves all payments for a specific account (both sent and received).

**Endpoint:** `GET /api/payments/account/{accountNumber}`

**Example:**
```
GET /api/payments/account/ACC001
```

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "transactionId": "123e4567-e89b-12d3-a456-426614174000",
    "fromAccount": "ACC001",
    "toAccount": "ACC002",
    "amount": 1000.00,
    "currency": "USD",
    "paymentType": "DOMESTIC_PAYMENT",
    "status": "COMPLETED",
    "description": "Payment for services",
    "failureReason": null,
    "createdAt": "2025-11-14T10:30:00",
    "updatedAt": "2025-11-14T10:30:05"
  }
]
```

### 4. Get All Payments
Retrieves all payment transactions (for admin/monitoring).

**Endpoint:** `GET /api/payments`

**Success Response (200 OK):**
Returns an array of all payment objects.

### 5. Health Check
Simple health check endpoint.

**Endpoint:** `GET /api/payments/health`

**Success Response (200 OK):**
```
Payment Processor is running
```

## Payment Processing Flow

1. **Account Validation**: Validates both source and destination accounts
2. **Fraud Detection**: Checks transaction for fraud patterns using mock fraud service
3. **Balance Verification**: Verifies sufficient balance in source account
4. **Payment Execution**: Processes the payment (debit source, credit destination)
5. **Status Update**: Updates payment status to COMPLETED or appropriate failure status

## Mock Services

### Fraud Service
- Flags transactions over $50,000 as potentially fraudulent
- Applies risk scoring based on transaction amount
- Has a 5% random fraud detection rate for testing
- Flags same-account transfers as suspicious

### Account Service
Mock accounts with pre-configured balances:
- `ACC001`: $100,000.00
- `ACC002`: $50,000.00
- `ACC003`: $25,000.00
- `ACC004`: $5,000.00
- `ACC005`: $1,000.00

## Example Usage with cURL

### Process a Payment
```bash
curl -X POST http://localhost:8081/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC001",
    "toAccount": "ACC002",
    "amount": 1000.00,
    "currency": "USD",
    "paymentType": "DOMESTIC_PAYMENT",
    "description": "Payment for services"
  }'
```

### Get Payment Status
```bash
curl http://localhost:8081/api/payments/{transactionId}
```

### Get Payments by Account
```bash
curl http://localhost:8081/api/payments/account/ACC001
```

## Error Scenarios

1. **Fraud Detection Failure**
   - Status: `FRAUD_CHECK_FAILED`
   - Reason: "Fraud detected: [specific reason]"

2. **Insufficient Balance**
   - Status: `INSUFFICIENT_BALANCE`
   - Reason: "Insufficient balance. Available: X, Required: Y"

3. **Invalid Account**
   - Status: `ACCOUNT_VALIDATION_FAILED`
   - Reason: "Source/Destination account validation failed"

4. **General Failure**
   - Status: `FAILED`
   - Reason: Specific error message

## Database Schema

The application uses PostgreSQL with the following schema:

```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    from_account VARCHAR(100) NOT NULL,
    to_account VARCHAR(100) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    description TEXT,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## Configuration

Database connection can be configured through environment variables:
- `SPRING_DATASOURCE_URL`: Database URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `SERVER_PORT`: Application port (default: 8081)
