package com.alok.payment.paymentprocessor.unit.service;

import com.alok.payment.paymentprocessor.dto.FraudCheckRequest;
import com.alok.payment.paymentprocessor.dto.FraudCheckResponse;
import com.alok.payment.paymentprocessor.service.FraudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FraudService Unit Tests")
class FraudServiceTest {

    private FraudService fraudService;

    @BeforeEach
    void setUp() {
        fraudService = new FraudService();
    }

    @Test
    @DisplayName("Should detect fraud for same account transfer")
    void testSameAccountTransferDetectsFraud() {
        FraudCheckRequest request = new FraudCheckRequest(
            "TXN-001",
            "ACC001",
            "ACC001",
            new BigDecimal("100.00"),
            "USD"
        );

        FraudCheckResponse response = fraudService.checkFraud(request);

        assertTrue(response.isFraudulent(), "Same account transfer should be flagged as fraudulent");
        assertEquals("TXN-001", response.getTransactionId());
        assertNotNull(response.getReason());
        assertTrue(response.getReason().contains("Same account"));
    }

    @Test
    @DisplayName("Should handle low amount transactions")
    void testLowAmountTransaction() {
        FraudCheckRequest request = new FraudCheckRequest(
            "TXN-002",
            "ACC001",
            "ACC002",
            new BigDecimal("50.00"),
            "USD"
        );

        FraudCheckResponse response = fraudService.checkFraud(request);

        assertNotNull(response);
        assertEquals("TXN-002", response.getTransactionId());
        assertTrue(response.getRiskScore() >= 0.0 && response.getRiskScore() <= 1.0);
    }

    @Test
    @DisplayName("Should calculate higher risk for high amount transactions")
    void testHighAmountTransaction() {
        FraudCheckRequest request = new FraudCheckRequest(
            "TXN-003",
            "ACC001",
            "ACC002",
            new BigDecimal("75000.00"),
            "USD"
        );

        FraudCheckResponse response = fraudService.checkFraud(request);

        assertNotNull(response);
        assertEquals("TXN-003", response.getTransactionId());
        assertTrue(response.getRiskScore() > 0.80, "High amount should have high risk score");
    }

    @Test
    @DisplayName("Should handle normal amount transactions")
    void testNormalAmountTransaction() {
        FraudCheckRequest request = new FraudCheckRequest(
            "TXN-004",
            "ACC001",
            "ACC002",
            new BigDecimal("5000.00"),
            "USD"
        );

        FraudCheckResponse response = fraudService.checkFraud(request);

        assertNotNull(response);
        assertEquals("TXN-004", response.getTransactionId());
        assertNotNull(response.getReason());
    }

    @Test
    @DisplayName("Should always return valid risk score")
    void testRiskScoreRange() {
        FraudCheckRequest request = new FraudCheckRequest(
            "TXN-005",
            "ACC001",
            "ACC002",
            new BigDecimal("1000.00"),
            "USD"
        );

        FraudCheckResponse response = fraudService.checkFraud(request);

        assertTrue(response.getRiskScore() >= 0.0, "Risk score should be >= 0");
        assertTrue(response.getRiskScore() <= 1.0, "Risk score should be <= 1");
    }

    @Test
    @DisplayName("Should handle null checks gracefully")
    void testNullHandling() {
        FraudCheckRequest request = new FraudCheckRequest();
        request.setTransactionId("TXN-006");
        request.setFromAccount("ACC001");
        request.setToAccount("ACC002");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");

        assertDoesNotThrow(() -> fraudService.checkFraud(request));
    }
}
