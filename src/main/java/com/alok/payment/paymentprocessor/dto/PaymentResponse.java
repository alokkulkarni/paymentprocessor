package com.alok.payment.paymentprocessor.dto;

import com.alok.payment.paymentprocessor.model.PaymentStatus;
import com.alok.payment.paymentprocessor.model.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    
    private String transactionId;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String currency;
    private PaymentType paymentType;
    private PaymentStatus status;
    private String message;
    private String failureReason;
    private LocalDateTime timestamp;

    public PaymentResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public PaymentResponse(String transactionId, String fromAccount, String toAccount, 
                          BigDecimal amount, String currency, PaymentType paymentType,
                          PaymentStatus status, String message) {
        this();
        this.transactionId = transactionId;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.currency = currency;
        this.paymentType = paymentType;
        this.status = status;
        this.message = message;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
