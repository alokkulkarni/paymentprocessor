package com.alok.payment.paymentprocessor.service;

import com.alok.payment.paymentprocessor.dto.FraudCheckResponse;
import com.alok.payment.paymentprocessor.model.Payment;
import com.alok.payment.paymentprocessor.model.PaymentAudit;
import com.alok.payment.paymentprocessor.model.PaymentStatus;
import com.alok.payment.paymentprocessor.repository.PaymentAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Payment Audit Service
 * Maintains comprehensive audit trail for all payment transactions
 * Records original payment details, fraud decisions, and final processing status
 */
@Service
public class PaymentAuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentAuditService.class);
    
    private final PaymentAuditRepository auditRepository;
    
    public PaymentAuditService(PaymentAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }
    
    /**
     * Creates audit record for completed payment with all processing details
     * 
     * @param payment The payment entity
     * @param fraudCheckResponse Fraud check results
     * @param sourceAccountValid Whether source account validation passed
     * @param destinationAccountValid Whether destination account validation passed
     * @param sufficientBalance Whether balance check passed
     * @param processingStartTime When processing started
     * @return Created audit record
     */
    @Transactional
    public PaymentAudit auditPayment(Payment payment, 
                                    FraudCheckResponse fraudCheckResponse,
                                    boolean sourceAccountValid,
                                    boolean destinationAccountValid,
                                    boolean sufficientBalance,
                                    LocalDateTime processingStartTime) {
        
        logger.info("Creating audit record for transaction: {}", payment.getTransactionId());
        
        if (payment == null || payment.getTransactionId() == null) {
            throw new IllegalArgumentException("Payment and transaction ID must not be null");
        }
        
        PaymentAudit audit = new PaymentAudit(
            payment.getTransactionId(),
            payment.getFromAccount(),
            payment.getToAccount(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getPaymentType()
        );
        
        // Original payment details
        audit.setDescription(payment.getDescription());
        audit.setPaymentInitiatedAt(payment.getCreatedAt());
        
        // Fraud decision details
        if (fraudCheckResponse != null) {
            audit.setFraudCheckPassed(!fraudCheckResponse.isFraudulent());
            audit.setFraudReason(fraudCheckResponse.getReason());
            audit.setFraudRiskScore(String.valueOf(fraudCheckResponse.getRiskScore()));
            audit.setFraudCheckAt(LocalDateTime.now());
        } else {
            audit.setFraudCheckPassed(null);
            audit.setFraudReason("Fraud check not performed");
        }
        
        // Account validation details
        audit.setSourceAccountValid(sourceAccountValid);
        audit.setDestinationAccountValid(destinationAccountValid);
        audit.setSufficientBalance(sufficientBalance);
        
        // Final processing status
        audit.setFinalStatus(payment.getStatus());
        audit.setFailureReason(payment.getFailureReason());
        audit.setCompletedAt(LocalDateTime.now());
        
        // Calculate processing time
        if (processingStartTime != null) {
            long processingTimeMs = Duration.between(processingStartTime, LocalDateTime.now()).toMillis();
            audit.setProcessingTimeMs(processingTimeMs);
        }
        
        PaymentAudit savedAudit = auditRepository.save(audit);
        
        logger.info("Audit record created successfully for transaction: {} with status: {}", 
                   payment.getTransactionId(), audit.getFinalStatus());
        
        return savedAudit;
    }
    
    /**
     * Creates audit record for failed payment (before fraud check)
     */
    @Transactional
    public PaymentAudit auditFailedPayment(Payment payment,
                                          boolean sourceAccountValid,
                                          boolean destinationAccountValid,
                                          LocalDateTime processingStartTime) {
        
        logger.info("Creating audit record for failed transaction: {}", payment.getTransactionId());
        
        PaymentAudit audit = new PaymentAudit(
            payment.getTransactionId(),
            payment.getFromAccount(),
            payment.getToAccount(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getPaymentType()
        );
        
        audit.setDescription(payment.getDescription());
        audit.setPaymentInitiatedAt(payment.getCreatedAt());
        audit.setSourceAccountValid(sourceAccountValid);
        audit.setDestinationAccountValid(destinationAccountValid);
        audit.setSufficientBalance(false);
        audit.setFinalStatus(payment.getStatus());
        audit.setFailureReason(payment.getFailureReason());
        audit.setCompletedAt(LocalDateTime.now());
        
        if (processingStartTime != null) {
            long processingTimeMs = Duration.between(processingStartTime, LocalDateTime.now()).toMillis();
            audit.setProcessingTimeMs(processingTimeMs);
        }
        
        return auditRepository.save(audit);
    }
    
    /**
     * Retrieves audit record by transaction ID
     */
    public Optional<PaymentAudit> getAuditByTransactionId(String transactionId) {
        logger.debug("Retrieving audit record for transaction: {}", transactionId);
        
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID must not be null or empty");
        }
        
        return auditRepository.findByTransactionId(transactionId);
    }
    
    /**
     * Retrieves all audit records for a specific account (sender or receiver)
     */
    public List<PaymentAudit> getAuditsByAccount(String accountNumber) {
        logger.debug("Retrieving audit records for account: {}", accountNumber);
        
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number must not be null or empty");
        }
        
        List<PaymentAudit> fromAudits = auditRepository.findByFromAccount(accountNumber);
        List<PaymentAudit> toAudits = auditRepository.findByToAccount(accountNumber);
        
        fromAudits.addAll(toAudits);
        return fromAudits;
    }
    
    /**
     * Retrieves all audit records with specific final status
     */
    public List<PaymentAudit> getAuditsByStatus(PaymentStatus status) {
        logger.debug("Retrieving audit records with status: {}", status);
        
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null");
        }
        
        return auditRepository.findByFinalStatus(status);
    }
    
    /**
     * Retrieves all audit records where fraud check failed
     */
    public List<PaymentAudit> getFraudulentPaymentAudits() {
        logger.debug("Retrieving audit records for fraudulent payments");
        return auditRepository.findByFraudCheckPassed(false);
    }
    
    /**
     * Retrieves audit records within a date range
     */
    public List<PaymentAudit> getAuditsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Retrieving audit records between {} and {}", startDate, endDate);
        
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date must not be null");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        
        return auditRepository.findByAuditedAtBetween(startDate, endDate);
    }
    
    /**
     * Calculates average processing time for successful payments
     */
    public Double calculateAverageProcessingTime() {
        logger.debug("Calculating average processing time");
        
        List<PaymentAudit> successfulAudits = auditRepository.findByFinalStatus(PaymentStatus.COMPLETED);
        
        if (successfulAudits.isEmpty()) {
            return 0.0;
        }
        
        double totalTime = successfulAudits.stream()
            .filter(audit -> audit.getProcessingTimeMs() != null)
            .mapToLong(PaymentAudit::getProcessingTimeMs)
            .average()
            .orElse(0.0);
        
        logger.info("Average processing time: {} ms", totalTime);
        return totalTime;
    }
    
    /**
     * Gets fraud detection success rate
     */
    public Double getFraudDetectionRate() {
        logger.debug("Calculating fraud detection rate");
        
        List<PaymentAudit> allAudits = (List<PaymentAudit>) auditRepository.findAll();
        
        if (allAudits.isEmpty()) {
            return 0.0;
        }
        
        long fraudulentCount = allAudits.stream()
            .filter(audit -> audit.getFraudCheckPassed() != null && !audit.getFraudCheckPassed())
            .count();
        
        double rate = (double) fraudulentCount / allAudits.size() * 100;
        
        logger.info("Fraud detection rate: {}%", rate);
        return rate;
    }
    
    /**
     * NEW: Get comprehensive audit analytics for a specific account
     * Provides detailed metrics for compliance and monitoring
     */
    public Map<String, Object> getAccountAuditAnalytics(String accountNumber) {
        logger.info("Generating audit analytics for account: {}", accountNumber);
        
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number must not be null or empty");
        }
        
        List<PaymentAudit> accountAudits = getAuditsByAccount(accountNumber);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("accountNumber", accountNumber);
        analytics.put("totalTransactions", accountAudits.size());
        
        // Status breakdown
        Map<PaymentStatus, Long> statusBreakdown = accountAudits.stream()
            .collect(Collectors.groupingBy(PaymentAudit::getFinalStatus, Collectors.counting()));
        analytics.put("statusBreakdown", statusBreakdown);
        
        // Fraud statistics
        long fraudDetected = accountAudits.stream()
            .filter(audit -> audit.getFraudCheckPassed() != null && !audit.getFraudCheckPassed())
            .count();
        analytics.put("fraudDetectedCount", fraudDetected);
        analytics.put("fraudPercentage", accountAudits.isEmpty() ? 0.0 : (double) fraudDetected / accountAudits.size() * 100);
        
        // Account validation failures
        long sourceValidationFailures = accountAudits.stream()
            .filter(audit -> audit.getSourceAccountValid() != null && !audit.getSourceAccountValid())
            .count();
        long destinationValidationFailures = accountAudits.stream()
            .filter(audit -> audit.getDestinationAccountValid() != null && !audit.getDestinationAccountValid())
            .count();
        analytics.put("sourceValidationFailures", sourceValidationFailures);
        analytics.put("destinationValidationFailures", destinationValidationFailures);
        
        // Balance issues
        long insufficientBalanceCount = accountAudits.stream()
            .filter(audit -> audit.getSufficientBalance() != null && !audit.getSufficientBalance())
            .count();
        analytics.put("insufficientBalanceCount", insufficientBalanceCount);
        
        // Performance metrics
        Double avgProcessingTime = accountAudits.stream()
            .filter(audit -> audit.getProcessingTimeMs() != null)
            .mapToLong(PaymentAudit::getProcessingTimeMs)
            .average()
            .orElse(0.0);
        analytics.put("averageProcessingTimeMs", avgProcessingTime);
        
        Long maxProcessingTime = accountAudits.stream()
            .filter(audit -> audit.getProcessingTimeMs() != null)
            .mapToLong(PaymentAudit::getProcessingTimeMs)
            .max()
            .orElse(0L);
        analytics.put("maxProcessingTimeMs", maxProcessingTime);
        
        logger.info("Analytics generated for account {}: {} total transactions", accountNumber, accountAudits.size());
        return analytics;
    }
    
    /**
     * NEW: Get high-risk transaction audits for compliance review
     * Returns audits that require manual review based on risk criteria
     */
    public List<PaymentAudit> getHighRiskTransactionAudits() {
        logger.info("Retrieving high-risk transaction audits for compliance review");
        
        List<PaymentAudit> allAudits = (List<PaymentAudit>) auditRepository.findAll();
        
        // Filter for high-risk criteria:
        // 1. Fraud risk score > 0.7
        // 2. Failed fraud check
        // 3. Processing time > 5000ms (potential timeout issues)
        List<PaymentAudit> highRiskAudits = allAudits.stream()
            .filter(audit -> {
                // High fraud risk score
                if (audit.getFraudRiskScore() != null) {
                    try {
                        double riskScore = Double.parseDouble(audit.getFraudRiskScore());
                        if (riskScore > 0.7) {
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid fraud risk score format: {}", audit.getFraudRiskScore());
                    }
                }
                
                // Failed fraud check
                if (audit.getFraudCheckPassed() != null && !audit.getFraudCheckPassed()) {
                    return true;
                }
                
                // Long processing time (potential issues)
                if (audit.getProcessingTimeMs() != null && audit.getProcessingTimeMs() > 5000) {
                    return true;
                }
                
                return false;
            })
            .collect(Collectors.toList());
        
        logger.info("Found {} high-risk transactions requiring review", highRiskAudits.size());
        return highRiskAudits;
    }
    
    /**
     * NEW: Generate daily audit summary for reporting
     * Provides aggregated statistics for a specific date
     */
    public Map<String, Object> getDailyAuditSummary(LocalDateTime date) {
        logger.info("Generating daily audit summary for date: {}", date);
        
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null");
        }
        
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        
        List<PaymentAudit> dailyAudits = getAuditsByDateRange(startOfDay, endOfDay);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("date", date.toLocalDate());
        summary.put("totalTransactions", dailyAudits.size());
        
        // Success/failure counts
        long successCount = dailyAudits.stream()
            .filter(audit -> audit.getFinalStatus() == PaymentStatus.COMPLETED)
            .count();
        long failureCount = dailyAudits.size() - successCount;
        summary.put("successfulTransactions", successCount);
        summary.put("failedTransactions", failureCount);
        summary.put("successRate", dailyAudits.isEmpty() ? 0.0 : (double) successCount / dailyAudits.size() * 100);
        
        // Fraud statistics
        long fraudCount = dailyAudits.stream()
            .filter(audit -> audit.getFraudCheckPassed() != null && !audit.getFraudCheckPassed())
            .count();
        summary.put("fraudDetectedCount", fraudCount);
        
        // Performance metrics
        Double avgTime = dailyAudits.stream()
            .filter(audit -> audit.getProcessingTimeMs() != null)
            .mapToLong(PaymentAudit::getProcessingTimeMs)
            .average()
            .orElse(0.0);
        summary.put("averageProcessingTimeMs", avgTime);
        
        // Top failure reasons
        Map<String, Long> failureReasons = dailyAudits.stream()
            .filter(audit -> audit.getFailureReason() != null)
            .collect(Collectors.groupingBy(PaymentAudit::getFailureReason, Collectors.counting()));
        summary.put("topFailureReasons", failureReasons);
        
        logger.info("Daily summary generated: {} transactions, {}% success rate", 
                   dailyAudits.size(), summary.get("successRate"));
        
        return summary;
    }
}
