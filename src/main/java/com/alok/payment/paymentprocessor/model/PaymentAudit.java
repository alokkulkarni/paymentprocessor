package com.alok.payment.paymentprocessor.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Audit Record
 * Stores comprehensive audit trail for payment processing including fraud checks and final status
 */
@Table("payment_audit")
public class PaymentAudit {
    
    @Id
    private Long id;
    
    private String transactionId;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String currency;
    private PaymentType paymentType;
    
    // Original Payment Details
    private String description;
    private LocalDateTime paymentInitiatedAt;
    
    // Fraud Decision Details
    private Boolean fraudCheckPassed;
    private String fraudReason;
    private String fraudRiskScore;
    private LocalDateTime fraudCheckAt;
    
    // Processing Details
    private PaymentStatus finalStatus;
    private String failureReason;
    private Long processingTimeMs;
    private LocalDateTime completedAt;
    
    // Account Validation Details
    private Boolean sourceAccountValid;
    private Boolean destinationAccountValid;
    private Boolean sufficientBalance;
    
    // Audit Metadata
    private String auditedBy;
    private LocalDateTime auditedAt;
    
    public PaymentAudit() {
        this.auditedAt = LocalDateTime.now();
        this.auditedBy = "SYSTEM";
    }

    public PaymentAudit(String transactionId, String fromAccount, String toAccount,
                       BigDecimal amount, String currency, PaymentType paymentType) {
        this();
        this.transactionId = transactionId;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.currency = currency;
        this.paymentType = paymentType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getPaymentInitiatedAt() {
        return paymentInitiatedAt;
    }

    public void setPaymentInitiatedAt(LocalDateTime paymentInitiatedAt) {
        this.paymentInitiatedAt = paymentInitiatedAt;
    }

    public Boolean getFraudCheckPassed() {
        return fraudCheckPassed;
    }

    public void setFraudCheckPassed(Boolean fraudCheckPassed) {
        this.fraudCheckPassed = fraudCheckPassed;
    }

    public String getFraudReason() {
        return fraudReason;
    }

    public void setFraudReason(String fraudReason) {
        this.fraudReason = fraudReason;
    }

    public String getFraudRiskScore() {
        return fraudRiskScore;
    }

    public void setFraudRiskScore(String fraudRiskScore) {
        this.fraudRiskScore = fraudRiskScore;
    }

    public LocalDateTime getFraudCheckAt() {
        return fraudCheckAt;
    }

    public void setFraudCheckAt(LocalDateTime fraudCheckAt) {
        this.fraudCheckAt = fraudCheckAt;
    }

    public PaymentStatus getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(PaymentStatus finalStatus) {
        this.finalStatus = finalStatus;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Boolean getSourceAccountValid() {
        return sourceAccountValid;
    }

    public void setSourceAccountValid(Boolean sourceAccountValid) {
        this.sourceAccountValid = sourceAccountValid;
    }

    public Boolean getDestinationAccountValid() {
        return destinationAccountValid;
    }

    public void setDestinationAccountValid(Boolean destinationAccountValid) {
        this.destinationAccountValid = destinationAccountValid;
    }

    public Boolean getSufficientBalance() {
        return sufficientBalance;
    }

    public void setSufficientBalance(Boolean sufficientBalance) {
        this.sufficientBalance = sufficientBalance;
    }

    public String getAuditedBy() {
        return auditedBy;
    }

    public void setAuditedBy(String auditedBy) {
        this.auditedBy = auditedBy;
    }

    public LocalDateTime getAuditedAt() {
        return auditedAt;
    }

    public void setAuditedAt(LocalDateTime auditedAt) {
        this.auditedAt = auditedAt;
    }

    @Override
    public String toString() {
        return "PaymentAudit{" +
                "id=" + id +
                ", transactionId='" + transactionId + '\'' +
                ", fromAccount='" + fromAccount + '\'' +
                ", toAccount='" + toAccount + '\'' +
                ", amount=" + amount +
                ", finalStatus=" + finalStatus +
                ", fraudCheckPassed=" + fraudCheckPassed +
                ", completedAt=" + completedAt +
                '}';
    }
}
