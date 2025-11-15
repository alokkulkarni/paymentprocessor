package com.alok.payment.paymentprocessor.service;

import com.alok.payment.paymentprocessor.dto.FraudCheckRequest;
import com.alok.payment.paymentprocessor.dto.FraudCheckResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Mock Fraud Detection Service
 * Simulates fraud detection by evaluating transaction patterns
 */
@Service
public class FraudService {
    
    private static final Logger logger = LoggerFactory.getLogger(FraudService.class);
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("10000");
    private static final BigDecimal SUSPICIOUS_AMOUNT_THRESHOLD = new BigDecimal("50000");
    private final Random random = new Random();
    private boolean deterministicMode = false;

    public void setDeterministicMode(boolean enabled) {
        this.deterministicMode = enabled;
        logger.info("Fraud service deterministic mode: {}", enabled);
    }

    public FraudCheckResponse checkFraud(FraudCheckRequest request) {
        logger.info("Performing fraud check for transaction: {}", request.getTransactionId());
        
        FraudCheckResponse response = new FraudCheckResponse();
        response.setTransactionId(request.getTransactionId());
        
        // Calculate risk score based on amount
        double riskScore = calculateRiskScore(request.getAmount());
        response.setRiskScore(riskScore);
        
        // Determine if transaction is fraudulent
        boolean isFraudulent = determineFraud(request, riskScore);
        response.setFraudulent(isFraudulent);
        
        if (isFraudulent) {
            response.setReason(generateFraudReason(request, riskScore));
            logger.warn("Fraud detected for transaction: {} - Reason: {}", 
                       request.getTransactionId(), response.getReason());
        } else {
            response.setReason("Transaction appears legitimate");
            logger.info("Fraud check passed for transaction: {}", request.getTransactionId());
        }
        
        return response;
    }
    
    private double calculateRiskScore(BigDecimal amount) {
        if (deterministicMode) {
            // Return fixed scores for deterministic testing
            if (amount.compareTo(SUSPICIOUS_AMOUNT_THRESHOLD) >= 0) {
                return 0.90; // Fixed high score
            } else if (amount.compareTo(HIGH_AMOUNT_THRESHOLD) >= 0) {
                return 0.50; // Fixed medium score
            } else {
                return 0.15; // Fixed low score
            }
        }
        
        // Normal mode with randomness
        if (amount.compareTo(SUSPICIOUS_AMOUNT_THRESHOLD) >= 0) {
            return 0.85 + (random.nextDouble() * 0.15); // 85-100%
        } else if (amount.compareTo(HIGH_AMOUNT_THRESHOLD) >= 0) {
            return 0.40 + (random.nextDouble() * 0.30); // 40-70%
        } else {
            return random.nextDouble() * 0.30; // 0-30%
        }
    }
    
    private boolean determineFraud(FraudCheckRequest request, double riskScore) {
        // Simulate fraud detection logic
        
        // Same account transfers are always suspicious
        if (request.getFromAccount().equals(request.getToAccount())) {
            return true;
        }
        
        // In deterministic mode, only check risk score without randomness
        if (deterministicMode) {
            // Only flag if risk score is very high (> 0.80)
            // For amounts >= SUSPICIOUS_AMOUNT_THRESHOLD, risk score is 85-100%, so this will be true
            return riskScore > 0.80;
        }
        
        // Normal mode with randomness
        // Very high amounts have 30% chance of being flagged
        if (request.getAmount().compareTo(SUSPICIOUS_AMOUNT_THRESHOLD) >= 0) {
            return random.nextDouble() < 0.30;
        }
        
        // Risk score based detection
        if (riskScore > 0.80) {
            return true;
        }
        
        // Random fraud simulation for realistic testing (5% of normal transactions)
        return random.nextDouble() < 0.05;
    }
    
    private String generateFraudReason(FraudCheckRequest request, double riskScore) {
        if (request.getFromAccount().equals(request.getToAccount())) {
            return "Same account transfer detected";
        }
        if (request.getAmount().compareTo(SUSPICIOUS_AMOUNT_THRESHOLD) >= 0) {
            return "Transaction amount exceeds suspicious threshold";
        }
        if (riskScore > 0.80) {
            return String.format("High risk score detected: %.2f", riskScore);
        }
        return "Suspicious transaction pattern detected";
    }
}
