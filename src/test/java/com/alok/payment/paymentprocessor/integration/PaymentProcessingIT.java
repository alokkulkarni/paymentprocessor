package com.alok.payment.paymentprocessor.integration;

import com.alok.payment.paymentprocessor.dto.PaymentRequest;
import com.alok.payment.paymentprocessor.dto.PaymentResponse;
import com.alok.payment.paymentprocessor.model.Payment;
import com.alok.payment.paymentprocessor.model.PaymentStatus;
import com.alok.payment.paymentprocessor.model.PaymentType;
import com.alok.payment.paymentprocessor.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment Processing Integration Tests")
class PaymentProcessingIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "/api/payments";
    }

    @Test
    @DisplayName("Should process domestic payment successfully with database persistence")
    void testSuccessfulDomesticPayment() {
        PaymentRequest request = new PaymentRequest();
        request.setFromAccount("ACC001");
        request.setToAccount("ACC002");
        request.setAmount(new BigDecimal("1000.00"));
        request.setCurrency("USD");
        request.setPaymentType(PaymentType.DOMESTIC_TRANSFER);
        request.setDescription("Integration test payment");

        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
                baseUrl, request, PaymentResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(PaymentStatus.COMPLETED, response.getBody().getStatus());
        assertNotNull(response.getBody().getTransactionId());

        // Verify database persistence
        Payment savedPayment = paymentRepository.findByTransactionId(
                response.getBody().getTransactionId()).orElse(null);
        assertNotNull(savedPayment);
        assertEquals("ACC001", savedPayment.getFromAccount());
        assertEquals("ACC002", savedPayment.getToAccount());
        assertEquals(PaymentStatus.COMPLETED, savedPayment.getStatus());
    }

    @Test
    @DisplayName("Should handle same account transfer as fraud")
    void testSameAccountFraudDetection() {
        PaymentRequest request = new PaymentRequest();
        request.setFromAccount("ACC001");
        request.setToAccount("ACC001"); // Same account
        request.setAmount(new BigDecimal("500.00"));
        request.setCurrency("USD");
        request.setPaymentType(PaymentType.DOMESTIC_TRANSFER);
        request.setDescription("Same account test");

        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
                baseUrl, request, PaymentResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(PaymentStatus.FRAUD_CHECK_FAILED, response.getBody().getStatus());
        assertTrue(response.getBody().getFailureReason().toLowerCase().contains("same account"));
    }

    @Test
    @DisplayName("Should handle insufficient balance scenario")
    void testInsufficientBalance() {
        PaymentRequest request = new PaymentRequest();
        request.setFromAccount("ACC005"); // Has balance of 1000.00
        request.setToAccount("ACC002");
        request.setAmount(new BigDecimal("5000.00")); // Exceeds balance but not suspicious amount
        request.setCurrency("USD");
        request.setPaymentType(PaymentType.INTERBANK_TRANSFER);
        request.setDescription("Insufficient balance test");

        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
                baseUrl, request, PaymentResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(PaymentStatus.INSUFFICIENT_BALANCE, response.getBody().getStatus());
        assertTrue(response.getBody().getFailureReason().toLowerCase().contains("insufficient"));
    }

    @Test
    @DisplayName("Should handle invalid source account")
    void testInvalidSourceAccount() {
        PaymentRequest request = new PaymentRequest();
        request.setFromAccount("INVALID999");
        request.setToAccount("ACC002");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setPaymentType(PaymentType.DOMESTIC_PAYMENT);
        request.setDescription("Invalid account test");

        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
                baseUrl, request, PaymentResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(PaymentStatus.ACCOUNT_VALIDATION_FAILED, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Should retrieve payment status by transaction ID")
    void testGetPaymentStatus() {
        // First create a payment
        PaymentRequest request = new PaymentRequest();
        request.setFromAccount("ACC001");
        request.setToAccount("ACC003");
        request.setAmount(new BigDecimal("250.00"));
        request.setCurrency("USD");
        request.setPaymentType(PaymentType.INTRABANK_TRANSFER);
        request.setDescription("Status test payment");

        ResponseEntity<PaymentResponse> createResponse = restTemplate.postForEntity(
                baseUrl, request, PaymentResponse.class);
        String transactionId = createResponse.getBody().getTransactionId();

        // Retrieve payment status
        ResponseEntity<PaymentResponse> statusResponse = restTemplate.getForEntity(
                baseUrl + "/" + transactionId, PaymentResponse.class);

        assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
        assertNotNull(statusResponse.getBody());
        assertEquals(transactionId, statusResponse.getBody().getTransactionId());
        assertEquals(PaymentStatus.COMPLETED, statusResponse.getBody().getStatus());
    }

    @Test
    @DisplayName("Should return 404 for non-existent transaction")
    void testGetNonExistentPayment() {
        ResponseEntity<PaymentResponse> response = restTemplate.getForEntity(
                baseUrl + "/NON-EXISTENT-ID", PaymentResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should retrieve payments by account number")
    void testGetPaymentsByAccount() {
        // Create test data - we already have payments in init.sql for ACC001
        ResponseEntity<Payment[]> response = restTemplate.getForEntity(
                baseUrl + "/account/ACC001", Payment[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
        
        // Verify all payments belong to the account
        for (Payment payment : response.getBody()) {
            assertTrue(payment.getFromAccount().equals("ACC001") || 
                      payment.getToAccount().equals("ACC001"));
        }
    }

    @Test
    @DisplayName("Should retrieve all payments")
    void testGetAllPayments() {
        ResponseEntity<Payment[]> response = restTemplate.getForEntity(
                baseUrl, Payment[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length >= 10); // We have 10 test records in init.sql
    }

    @Test
    @DisplayName("Should verify health check endpoint")
    void testHealthCheck() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/health", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Payment Processor is running", response.getBody());
    }

    @Test
    @DisplayName("Should handle multiple sequential payments correctly")
    void testSequentialPayments() {
        for (int i = 0; i < 3; i++) {
            PaymentRequest request = new PaymentRequest();
            request.setFromAccount("ACC001");
            request.setToAccount("ACC002");
            request.setAmount(new BigDecimal("100.00"));
            request.setCurrency("USD");
            request.setPaymentType(PaymentType.DOMESTIC_TRANSFER);
            request.setDescription("Sequential payment " + i);

            ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
                    baseUrl, request, PaymentResponse.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(PaymentStatus.COMPLETED, response.getBody().getStatus());
        }

        // Verify all payments were saved
        List<Payment> payments = (List<Payment>) paymentRepository.findAll();
        assertTrue(payments.size() >= 13); // 10 from init.sql + 3 new
    }

    @Test
    @DisplayName("Should handle interbank transfer successfully")
    void testInterbankTransfer() {
        PaymentRequest request = new PaymentRequest();
        request.setFromAccount("ACC002");
        request.setToAccount("ACC003");
        request.setAmount(new BigDecimal("750.00"));
        request.setCurrency("USD");
        request.setPaymentType(PaymentType.INTERBANK_TRANSFER);
        request.setDescription("Interbank transfer test");

        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
                baseUrl, request, PaymentResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(PaymentStatus.COMPLETED, response.getBody().getStatus());
        assertEquals(PaymentType.INTERBANK_TRANSFER, response.getBody().getPaymentType());
    }

    @Test
    @DisplayName("Should verify test data from init.sql is loaded")
    void testInitialDataLoaded() {
        // Verify we can find the test payments from init.sql
        Payment testPayment = paymentRepository.findByTransactionId("TEST-TXN-001").orElse(null);
        
        assertNotNull(testPayment, "Test data from init.sql should be loaded");
        assertEquals("ACC001", testPayment.getFromAccount());
        assertEquals("ACC002", testPayment.getToAccount());
        assertEquals(PaymentStatus.COMPLETED, testPayment.getStatus());
    }
}
