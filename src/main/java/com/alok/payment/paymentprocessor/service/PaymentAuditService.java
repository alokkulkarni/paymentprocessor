package com.alok.payment.paymentprocessor.service;

import com.alok.payment.paymentprocessor.model.Payment;
import com.alok.payment.paymentprocessor.model.PaymentAudit;
import com.alok.payment.paymentprocessor.model.PaymentStatus;
import com.alok.payment.paymentprocessor.repository.PaymentAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment Audit Service
 * Manages audit logging for payment transactions
 */
@Service
public class PaymentAuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentAuditService.class);
    
    private final PaymentAuditRepository auditRepository;
    
    public PaymentAuditService(PaymentAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }
    
    /**
     * Log payment creation audit
     */
    @Transactional
    public PaymentAudit logPaymentCreated(Payment payment, String performedBy) {
        logger.info("Logging payment creation audit for transaction: {}", payment.getTransactionId());
        
        PaymentAudit audit = new PaymentAudit(
            payment.getTransactionId(),
            "PAYMENT_CREATED",
            null,
            payment.getStatus(),
            payment.getFromAccount(),
            payment.getToAccount(),
            payment.getAmount(),
            payment.getCurrency(),
            performedBy,
            "Payment initiated: " + payment.getDescription()
        );
        
        return auditRepository.save(audit);
    }
    
    /**
     * Log payment status change audit
     */
    @Transactional
    public PaymentAudit logStatusChange(Payment payment, PaymentStatus oldStatus, String performedBy) {
        logger.info("Logging status change audit for transaction: {} from {} to {}", 
                   payment.getTransactionId(), oldStatus, payment.getStatus());
        
        PaymentAudit audit = new PaymentAudit(
            payment.getTransactionId(),
            "STATUS_CHANGE",
            oldStatus,
            payment.getStatus(),
            payment.getFromAccount(),
            payment.getToAccount(),
            payment.getAmount(),
            payment.getCurrency(),
            performedBy,
            String.format("Status changed from %s to %s", oldStatus, payment.getStatus())
        );
        
        return auditRepository.save(audit);
    }
    
    /**
     * Log payment completion audit
     */
    @Transactional
    public PaymentAudit logPaymentCompleted(Payment payment, String performedBy) {
        logger.info("Logging payment completion audit for transaction: {}", payment.getTransactionId());
        
        PaymentAudit audit = new PaymentAudit(
            payment.getTransactionId(),
            "PAYMENT_COMPLETED",
            PaymentStatus.PROCESSING,
            PaymentStatus.COMPLETED,
            payment.getFromAccount(),
            payment.getToAccount(),
            payment.getAmount(),
            payment.getCurrency(),
            performedBy,
            "Payment completed successfully"
        );
        
        return auditRepository.save(audit);
    }
    
    /**
     * Log payment failure audit
     */
    @Transactional
    public PaymentAudit logPaymentFailed(Payment payment, PaymentStatus oldStatus, String reason, String performedBy) {
        logger.info("Logging payment failure audit for transaction: {}", payment.getTransactionId());
        
        PaymentAudit audit = new PaymentAudit(
            payment.getTransactionId(),
            "PAYMENT_FAILED",
            oldStatus,
            payment.getStatus(),
            payment.getFromAccount(),
            payment.getToAccount(),
            payment.getAmount(),
            payment.getCurrency(),
            performedBy,
            "Payment failed: " + reason
        );
        
        return auditRepository.save(audit);
    }
    
    /**
     * Get audit trail for a specific transaction
     */
    public List<PaymentAudit> getAuditTrail(String transactionId) {
        logger.info("Retrieving audit trail for transaction: {}", transactionId);
        return auditRepository.findByTransactionId(transactionId);
    }
    
    /**
     * Get audit records for a specific account
     */
    public List<PaymentAudit> getAuditByAccount(String accountNumber) {
        logger.info("Retrieving audit records for account: {}", accountNumber);
        return auditRepository.findByAccount(accountNumber);
    }
    
    /**
     * Get audit records by action type
     */
    public List<PaymentAudit> getAuditByAction(String action) {
        logger.info("Retrieving audit records for action: {}", action);
        return auditRepository.findByAction(action);
    }
    
    /**
     * Get audit records within a date range
     */
    public List<PaymentAudit> getAuditByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Retrieving audit records between {} and {}", startDate, endDate);
        return auditRepository.findByAuditTimestampBetween(startDate, endDate);
    }
    
    /**
     * Get recent audit records
     */
    public List<PaymentAudit> getRecentAudits(int limit) {
        logger.info("Retrieving {} most recent audit records", limit);
        return auditRepository.findRecentAudits(limit);
    }
}
