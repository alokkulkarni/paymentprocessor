package com.alok.payment.paymentprocessor.integration;

import com.alok.payment.paymentprocessor.dto.PaymentRequest;
import com.alok.payment.paymentprocessor.dto.PaymentResponse;
import com.alok.payment.paymentprocessor.model.PaymentStatus;
import com.alok.payment.paymentprocessor.model.PaymentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Database Connection and Failover Integration Tests")
class DatabaseConnectionIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Should verify database connection is established")
    void testDatabaseConnection() {
        assertTrue(AbstractIntegrationTest.postgres.isRunning(), 
                  "PostgreSQL container should be running");
        assertNotNull(AbstractIntegrationTest.postgres.getJdbcUrl(), 
                     "JDBC URL should be available");
    }

    @Test
    @DisplayName("Should handle database operations successfully")
    void testDatabaseOperations() {
        PaymentRequest request = new PaymentRequest();
        request.setFromAccount("ACC001");
        request.setToAccount("ACC002");
        request.setAmount(new BigDecimal("500.00"));
        request.setCurrency("USD");
        request.setPaymentType(PaymentType.DOMESTIC_TRANSFER);
        request.setDescription("Database operation test");

        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
                "/api/payments", request, PaymentResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verify data was persisted by retrieving it
        String transactionId = response.getBody().getTransactionId();
        ResponseEntity<PaymentResponse> retrieveResponse = restTemplate.getForEntity(
                "/api/payments/" + transactionId, PaymentResponse.class);
        
        assertEquals(HttpStatus.OK, retrieveResponse.getStatusCode());
        assertEquals(transactionId, retrieveResponse.getBody().getTransactionId());
    }

    @Test
    @DisplayName("Should maintain data integrity across transactions")
    void testDataIntegrity() {
        // Create multiple payments with small amounts
        int paymentCount = 3;
        for (int i = 0; i < paymentCount; i++) {
            PaymentRequest request = new PaymentRequest();
            request.setFromAccount("ACC002");
            request.setToAccount("ACC003");
            request.setAmount(new BigDecimal("50.00"));
            request.setCurrency("USD");
            request.setPaymentType(PaymentType.INTRABANK_TRANSFER);
            request.setDescription("Integrity test " + i);

            ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
                    "/api/payments", request, PaymentResponse.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(PaymentStatus.COMPLETED, response.getBody().getStatus());
        }

        // Verify all payments are accessible
        ResponseEntity<PaymentResponse[]> allResponse = restTemplate.getForEntity(
                "/api/payments", PaymentResponse[].class);
        
        assertEquals(HttpStatus.OK, allResponse.getStatusCode());
        assertTrue(allResponse.getBody().length >= paymentCount);
    }

    @Test
    @DisplayName("Should handle concurrent payment requests")
    void testConcurrentRequests() throws InterruptedException {
        Thread[] threads = new Thread[3];
        boolean[] results = new boolean[3];

        for (int i = 0; i < 3; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                PaymentRequest request = new PaymentRequest();
                request.setFromAccount("ACC001");
                request.setToAccount("ACC002");
                request.setAmount(new BigDecimal("50.00"));
                request.setCurrency("USD");
                request.setPaymentType(PaymentType.DOMESTIC_PAYMENT);
                request.setDescription("Concurrent test " + index);

                try {
                    ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
                            "/api/payments", request, PaymentResponse.class);
                    results[index] = response.getStatusCode() == HttpStatus.OK;
                } catch (Exception e) {
                    results[index] = false;
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify all requests succeeded
        for (boolean result : results) {
            assertTrue(result, "All concurrent requests should succeed");
        }
    }

    @Test
    @DisplayName("Should verify container health")
    void testContainerHealth() {
        PostgreSQLContainer<?> container = AbstractIntegrationTest.postgres;
        
        assertTrue(container.isRunning(), "Container should be running");
        assertEquals("paymentprocessor", container.getDatabaseName());
        assertEquals("test", container.getUsername());
        assertNotNull(container.getJdbcUrl(), "JDBC URL should be available");
    }

    @Test
    @DisplayName("Should handle large number of sequential operations")
    void testSequentialOperations() {
        int operationCount = 10;
        int successCount = 0;

        for (int i = 0; i < operationCount; i++) {
            PaymentRequest request = new PaymentRequest();
            request.setFromAccount("ACC003");
            request.setToAccount("ACC004");
            request.setAmount(new BigDecimal("10.00"));
            request.setCurrency("USD");
            request.setPaymentType(PaymentType.DOMESTIC_TRANSFER);
            request.setDescription("Sequential operation " + i);

            ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
                    "/api/payments", request, PaymentResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && 
                response.getBody() != null && 
                response.getBody().getStatus() == PaymentStatus.COMPLETED) {
                successCount++;
            }
        }

        // At least 80% should succeed (accounting for random fraud checks)
        assertTrue(successCount >= 8, 
                  "At least 8 out of " + operationCount + " operations should succeed, got: " + successCount);
    }

    @Test
    @DisplayName("Should verify database schema is properly initialized")
    void testSchemaInitialization() {
        // Verify we can query the test data loaded from init.sql
        ResponseEntity<PaymentResponse> response = restTemplate.getForEntity(
                "/api/payments/TEST-TXN-001", PaymentResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TEST-TXN-001", response.getBody().getTransactionId());
    }

    @Test
    @DisplayName("Should handle payment status transitions")
    void testPaymentStatusTransitions() {
        // Create a payment that will go through multiple states
        PaymentRequest request = new PaymentRequest();
        request.setFromAccount("ACC001");
        request.setToAccount("ACC002");
        request.setAmount(new BigDecimal("300.00"));
        request.setCurrency("USD");
        request.setPaymentType(PaymentType.DOMESTIC_TRANSFER);
        request.setDescription("Status transition test");

        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
                "/api/payments", request, PaymentResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Payment should transition from PENDING -> PROCESSING -> COMPLETED
        PaymentResponse payment = response.getBody();
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        
        // Verify final state is persisted
        ResponseEntity<PaymentResponse> verifyResponse = restTemplate.getForEntity(
                "/api/payments/" + payment.getTransactionId(), PaymentResponse.class);
        
        assertEquals(PaymentStatus.COMPLETED, verifyResponse.getBody().getStatus());
    }
}
