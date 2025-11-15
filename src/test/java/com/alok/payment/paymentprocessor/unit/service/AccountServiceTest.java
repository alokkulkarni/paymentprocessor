package com.alok.payment.paymentprocessor.unit.service;

import com.alok.payment.paymentprocessor.dto.AccountBalanceRequest;
import com.alok.payment.paymentprocessor.dto.AccountBalanceResponse;
import com.alok.payment.paymentprocessor.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AccountService Unit Tests")
class AccountServiceTest {

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService();
    }

    @Test
    @DisplayName("Should validate existing account successfully")
    void testValidateExistingAccount() {
        AccountBalanceResponse response = accountService.validateAccount("ACC001");

        assertTrue(response.isValid(), "ACC001 should be valid");
        assertEquals("ACC001", response.getAccountNumber());
        assertNotNull(response.getAvailableBalance());
        assertEquals(new BigDecimal("100000.00"), response.getAvailableBalance());
    }

    @Test
    @DisplayName("Should return account balance for mock accounts")
    void testGetAccountBalance() {
        AccountBalanceResponse response = accountService.validateAccount("ACC002");

        assertTrue(response.isValid());
        assertEquals(new BigDecimal("50000.00"), response.getAvailableBalance());
    }

    @Test
    @DisplayName("Should detect sufficient balance")
    void testSufficientBalance() {
        AccountBalanceRequest request = new AccountBalanceRequest("ACC001", new BigDecimal("1000.00"));

        AccountBalanceResponse response = accountService.checkBalance(request);

        assertTrue(response.isValid());
        assertTrue(response.isSufficientBalance());
        assertEquals("Sufficient balance available", response.getMessage());
    }

    @Test
    @DisplayName("Should detect insufficient balance")
    void testInsufficientBalance() {
        AccountBalanceRequest request = new AccountBalanceRequest("ACC005", new BigDecimal("50000.00"));

        AccountBalanceResponse response = accountService.checkBalance(request);

        assertTrue(response.isValid());
        assertFalse(response.isSufficientBalance());
        assertTrue(response.getMessage().contains("Insufficient balance"));
    }

    @Test
    @DisplayName("Should handle invalid account number")
    void testInvalidAccount() {
        AccountBalanceResponse response = accountService.validateAccount("");

        assertFalse(response.isValid());
        assertEquals("Invalid account number", response.getMessage());
    }

    @Test
    @DisplayName("Should handle null account number")
    void testNullAccount() {
        AccountBalanceResponse response = accountService.validateAccount(null);

        assertFalse(response.isValid());
    }

    @Test
    @DisplayName("Should deduct balance correctly")
    void testDeductBalance() {
        BigDecimal initialBalance = new BigDecimal("100000.00");
        BigDecimal deductAmount = new BigDecimal("1000.00");
        
        accountService.deductBalance("ACC001", deductAmount);
        
        // Verify balance is deducted by checking the updated balance
        AccountBalanceResponse response = accountService.validateAccount("ACC001");
        assertEquals(initialBalance.subtract(deductAmount), response.getAvailableBalance());
    }

    @Test
    @DisplayName("Should add balance correctly")
    void testAddBalance() {
        BigDecimal addAmount = new BigDecimal("500.00");
        
        AccountBalanceResponse beforeResponse = accountService.validateAccount("ACC002");
        BigDecimal balanceBefore = beforeResponse.getAvailableBalance();
        
        accountService.addBalance("ACC002", addAmount);
        
        AccountBalanceResponse afterResponse = accountService.validateAccount("ACC002");
        assertEquals(balanceBefore.add(addAmount), afterResponse.getAvailableBalance());
    }

    @Test
    @DisplayName("Should handle valid account pattern")
    void testValidAccountPattern() {
        AccountBalanceResponse response = accountService.validateAccount("ACC999");

        // Account with valid pattern should be validated
        assertNotNull(response);
        assertEquals("ACC999", response.getAccountNumber());
    }

    @Test
    @DisplayName("Should check balance for exact amount")
    void testExactBalanceCheck() {
        AccountBalanceRequest request = new AccountBalanceRequest("ACC005", new BigDecimal("1000.00"));

        AccountBalanceResponse response = accountService.checkBalance(request);

        assertTrue(response.isValid());
        assertTrue(response.isSufficientBalance());
    }
}
