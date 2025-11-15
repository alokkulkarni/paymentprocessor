package com.alok.payment.paymentprocessor.service;

import com.alok.payment.paymentprocessor.dto.AccountBalanceRequest;
import com.alok.payment.paymentprocessor.dto.AccountBalanceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Mock Account Service
 * Simulates account validation and balance checking
 */
@Service
public class AccountService {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private final Map<String, BigDecimal> mockAccountBalances;
    private final Random random = new Random();
    
    public AccountService() {
        // Initialize some mock accounts with balances
        mockAccountBalances = new HashMap<>();
        mockAccountBalances.put("ACC001", new BigDecimal("100000.00"));
        mockAccountBalances.put("ACC002", new BigDecimal("50000.00"));
        mockAccountBalances.put("ACC003", new BigDecimal("25000.00"));
        mockAccountBalances.put("ACC004", new BigDecimal("5000.00"));
        mockAccountBalances.put("ACC005", new BigDecimal("1000.00"));
    }
    
    public AccountBalanceResponse validateAccount(String accountNumber) {
        logger.info("Validating account: {}", accountNumber);
        
        AccountBalanceResponse response = new AccountBalanceResponse();
        response.setAccountNumber(accountNumber);
        
        // Check if account exists (either in mock data or follows valid pattern)
        boolean isValid = isAccountValid(accountNumber);
        response.setValid(isValid);
        
        if (isValid) {
            BigDecimal balance = getAccountBalance(accountNumber);
            response.setAvailableBalance(balance);
            response.setSufficientBalance(true); // Just validation, not checking amount yet
            response.setMessage("Account is valid");
            logger.info("Account {} validated successfully with balance: {}", accountNumber, balance);
        } else {
            response.setSufficientBalance(false);
            response.setMessage("Invalid account number");
            logger.warn("Account validation failed for: {}", accountNumber);
        }
        
        return response;
    }
    
    public AccountBalanceResponse checkBalance(AccountBalanceRequest request) {
        logger.info("Checking balance for account: {} for amount: {}", 
                   request.getAccountNumber(), request.getAmount());
        
        AccountBalanceResponse response = new AccountBalanceResponse();
        response.setAccountNumber(request.getAccountNumber());
        
        // First validate account
        if (!isAccountValid(request.getAccountNumber())) {
            response.setValid(false);
            response.setSufficientBalance(false);
            response.setMessage("Invalid account number");
            logger.warn("Invalid account: {}", request.getAccountNumber());
            return response;
        }
        
        response.setValid(true);
        BigDecimal balance = getAccountBalance(request.getAccountNumber());
        response.setAvailableBalance(balance);
        
        // Check if sufficient balance
        if (balance.compareTo(request.getAmount()) >= 0) {
            response.setSufficientBalance(true);
            response.setMessage("Sufficient balance available");
            logger.info("Sufficient balance check passed for account: {}", request.getAccountNumber());
        } else {
            response.setSufficientBalance(false);
            response.setMessage(String.format("Insufficient balance. Available: %s, Required: %s", 
                                             balance, request.getAmount()));
            logger.warn("Insufficient balance for account: {}. Available: {}, Required: {}", 
                       request.getAccountNumber(), balance, request.getAmount());
        }
        
        return response;
    }
    
    private boolean isAccountValid(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return false;
        }
        
        // Check if account exists in mock data
        if (mockAccountBalances.containsKey(accountNumber)) {
            return true;
        }
        
        // Allow accounts that match pattern ACCxxx (simulating valid account format)
        // This provides deterministic validation for testing
        return accountNumber.matches("ACC\\d{3,}");
    }
    
    private BigDecimal getAccountBalance(String accountNumber) {
        // Return balance from mock data if exists
        if (mockAccountBalances.containsKey(accountNumber)) {
            return mockAccountBalances.get(accountNumber);
        }
        
        // Generate a random balance for unknown accounts
        double randomBalance = 1000 + (random.nextDouble() * 99000); // Between 1K and 100K
        return BigDecimal.valueOf(randomBalance).setScale(2, RoundingMode.HALF_UP);
    }
    
    public void deductBalance(String accountNumber, BigDecimal amount) {
        logger.info("Deducting {} from account: {}", amount, accountNumber);
        if (mockAccountBalances.containsKey(accountNumber)) {
            BigDecimal currentBalance = mockAccountBalances.get(accountNumber);
            mockAccountBalances.put(accountNumber, currentBalance.subtract(amount));
        }
    }
    
    public void addBalance(String accountNumber, BigDecimal amount) {
        logger.info("Adding {} to account: {}", amount, accountNumber);
        if (mockAccountBalances.containsKey(accountNumber)) {
            BigDecimal currentBalance = mockAccountBalances.get(accountNumber);
            mockAccountBalances.put(accountNumber, currentBalance.add(amount));
        } else {
            mockAccountBalances.put(accountNumber, amount);
        }
    }
    
    /**
     * Reset account balances to initial state
     * Used for testing to ensure consistent state
     */
    public void resetBalances() {
        logger.info("Resetting account balances to initial state");
        mockAccountBalances.clear();
        mockAccountBalances.put("ACC001", new BigDecimal("100000.00"));
        mockAccountBalances.put("ACC002", new BigDecimal("50000.00"));
        mockAccountBalances.put("ACC003", new BigDecimal("25000.00"));
        mockAccountBalances.put("ACC004", new BigDecimal("5000.00"));
        mockAccountBalances.put("ACC005", new BigDecimal("1000.00"));
    }
}
