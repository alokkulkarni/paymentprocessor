package com.alok.payment.paymentprocessor.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Audit Entity
 * Records audit trail for all payment transactions
 */
@Table("payment_audit")
public class PaymentAudit {
    
    @Id
    private Long id;
    
    private String transactionId;
    private String action;
    private PaymentStatus oldStatus;
    private PaymentStatus newStatus;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String currency;
    private String performedBy;
    private String details;
    private LocalDateTime auditTimestamp;
    
    public PaymentAudit() {
        this.auditTimestamp = LocalDateTime.now();
    }
    
    public PaymentAudit(String transactionId, String action, PaymentStatus oldStatus, 
                       PaymentStatus newStatus, String fromAccount, String toAccount,
                       BigDecimal amount, String currency, String performedBy, String details) {
        this();
        this.transactionId = transactionId;
        this.action = action;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.currency = currency;
        this.performedBy = performedBy;
        this.details = details;
    }
    
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
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public PaymentStatus getOldStatus() {
        return oldStatus;
    }
    
    public void setOldStatus(PaymentStatus oldStatus) {
        this.oldStatus = oldStatus;
    }
    
    public PaymentStatus getNewStatus() {
        return newStatus;
    }
    
    public void setNewStatus(PaymentStatus newStatus) {
        this.newStatus = newStatus;
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
    
    public String getPerformedBy() {
        return performedBy;
    }
    
    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public LocalDateTime getAuditTimestamp() {
        return auditTimestamp;
    }
    
    public void setAuditTimestamp(LocalDateTime auditTimestamp) {
        this.auditTimestamp = auditTimestamp;
    }
}
