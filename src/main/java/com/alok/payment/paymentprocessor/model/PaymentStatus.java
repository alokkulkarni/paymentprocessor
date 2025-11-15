package com.alok.payment.paymentprocessor.model;

public enum PaymentStatus {
    PENDING,
    FRAUD_CHECK_FAILED,
    INSUFFICIENT_BALANCE,
    ACCOUNT_VALIDATION_FAILED,
    PROCESSING,
    COMPLETED,
    FAILED
}
