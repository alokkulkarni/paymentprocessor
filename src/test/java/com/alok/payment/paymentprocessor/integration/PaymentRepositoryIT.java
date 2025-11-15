package com.alok.payment.paymentprocessor.integration;

import com.alok.payment.paymentprocessor.model.Payment;
import com.alok.payment.paymentprocessor.model.PaymentStatus;
import com.alok.payment.paymentprocessor.model.PaymentType;
import com.alok.payment.paymentprocessor.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment Repository Integration Tests")
class PaymentRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("Should save and retrieve payment from database")
    void testSaveAndRetrievePayment() {
        Payment payment = new Payment(
            "TEST-TXN-REPO-001",
            "ACC001",
            "ACC002",
            new BigDecimal("1500.00"),
            "USD",
            PaymentType.DOMESTIC_TRANSFER,
            "Repository test payment"
        );
        payment.setStatus(PaymentStatus.COMPLETED);

        Payment saved = paymentRepository.save(payment);

        assertNotNull(saved.getId());
        assertEquals("TEST-TXN-REPO-001", saved.getTransactionId());
        
        // Retrieve and verify
        Optional<Payment> retrieved = paymentRepository.findById(saved.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("ACC001", retrieved.get().getFromAccount());
        assertEquals(PaymentStatus.COMPLETED, retrieved.get().getStatus());
    }

    @Test
    @DisplayName("Should find payment by transaction ID")
    void testFindByTransactionId() {
        // Use test data from init.sql
        Optional<Payment> payment = paymentRepository.findByTransactionId("TEST-TXN-002");
        
        assertTrue(payment.isPresent());
        assertEquals("ACC002", payment.get().getFromAccount());
        assertEquals("ACC003", payment.get().getToAccount());
        assertEquals(new BigDecimal("500.00"), payment.get().getAmount());
    }

    @Test
    @DisplayName("Should find payments by from account")
    void testFindByFromAccount() {
        List<Payment> payments = (List<Payment>) paymentRepository.findByFromAccount("ACC001");
        
        assertNotNull(payments);
        assertTrue(payments.size() > 0);
        
        // Verify all payments are from ACC001
        for (Payment payment : payments) {
            assertEquals("ACC001", payment.getFromAccount());
        }
    }

    @Test
    @DisplayName("Should find payments by to account")
    void testFindByToAccount() {
        List<Payment> payments = (List<Payment>) paymentRepository.findByToAccount("ACC002");
        
        assertNotNull(payments);
        assertTrue(payments.size() > 0);
        
        // Verify all payments are to ACC002
        for (Payment payment : payments) {
            assertEquals("ACC002", payment.getToAccount());
        }
    }

    @Test
    @DisplayName("Should find payments by status")
    void testFindByStatus() {
        List<Payment> completedPayments = (List<Payment>) paymentRepository.findByStatus(PaymentStatus.COMPLETED);
        
        assertNotNull(completedPayments);
        assertTrue(completedPayments.size() >= 4); // At least 4 completed in init.sql
        
        // Verify all have COMPLETED status
        for (Payment payment : completedPayments) {
            assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        }
    }

    @Test
    @DisplayName("Should find payments by account (from or to)")
    void testFindByAccount() {
        List<Payment> payments = (List<Payment>) paymentRepository.findByAccount("ACC001");
        
        assertNotNull(payments);
        assertTrue(payments.size() > 0);
        
        // Verify all payments involve ACC001
        for (Payment payment : payments) {
            assertTrue(payment.getFromAccount().equals("ACC001") || 
                      payment.getToAccount().equals("ACC001"));
        }
    }

    @Test
    @DisplayName("Should update payment status")
    void testUpdatePaymentStatus() {
        // Create a pending payment
        Payment payment = new Payment(
            "TEST-TXN-UPDATE-001",
            "ACC002",
            "ACC003",
            new BigDecimal("500.00"),
            "USD",
            PaymentType.DOMESTIC_PAYMENT,
            "Update test"
        );
        payment.setStatus(PaymentStatus.PENDING);
        
        Payment saved = paymentRepository.save(payment);
        
        // Update status
        saved.setStatus(PaymentStatus.COMPLETED);
        Payment updated = paymentRepository.save(saved);
        
        assertEquals(PaymentStatus.COMPLETED, updated.getStatus());
        
        // Verify in database
        Optional<Payment> retrieved = paymentRepository.findByTransactionId("TEST-TXN-UPDATE-001");
        assertTrue(retrieved.isPresent());
        assertEquals(PaymentStatus.COMPLETED, retrieved.get().getStatus());
    }

    @Test
    @DisplayName("Should handle multiple payment types")
    void testMultiplePaymentTypes() {
        for (PaymentType type : PaymentType.values()) {
            Payment payment = new Payment(
                "TEST-TXN-TYPE-" + type.name(),
                "ACC001",
                "ACC002",
                new BigDecimal("100.00"),
                "USD",
                type,
                "Payment type test: " + type.name()
            );
            payment.setStatus(PaymentStatus.COMPLETED);
            
            Payment saved = paymentRepository.save(payment);
            assertNotNull(saved.getId());
            assertEquals(type, saved.getPaymentType());
        }
    }

    @Test
    @DisplayName("Should handle payment with failure reason")
    void testPaymentWithFailureReason() {
        Payment payment = new Payment(
            "TEST-TXN-FAIL-001",
            "ACC001",
            "ACC002",
            new BigDecimal("75000.00"),
            "USD",
            PaymentType.INTERBANK_TRANSFER,
            "Failure test"
        );
        payment.setStatus(PaymentStatus.FRAUD_CHECK_FAILED);
        payment.setFailureReason("High risk transaction detected");
        
        Payment saved = paymentRepository.save(payment);
        
        assertNotNull(saved.getId());
        assertEquals(PaymentStatus.FRAUD_CHECK_FAILED, saved.getStatus());
        assertEquals("High risk transaction detected", saved.getFailureReason());
    }

    @Test
    @DisplayName("Should retrieve all payments with count verification")
    void testFindAllPayments() {
        List<Payment> allPayments = (List<Payment>) paymentRepository.findAll();
        
        assertNotNull(allPayments);
        assertTrue(allPayments.size() >= 10); // At least 10 from init.sql
    }

    @Test
    @DisplayName("Should handle decimal amounts correctly")
    void testDecimalAmounts() {
        Payment payment = new Payment(
            "TEST-TXN-DECIMAL-001",
            "ACC002",
            "ACC003",
            new BigDecimal("1234.56"),
            "USD",
            PaymentType.DOMESTIC_TRANSFER,
            "Decimal amount test"
        );
        payment.setStatus(PaymentStatus.COMPLETED);
        
        paymentRepository.save(payment);
        
        // Retrieve and verify amount precision
        Optional<Payment> retrieved = paymentRepository.findByTransactionId("TEST-TXN-DECIMAL-001");
        assertTrue(retrieved.isPresent());
        assertEquals(new BigDecimal("1234.56"), retrieved.get().getAmount());
    }

    @Test
    @DisplayName("Should handle empty result sets")
    void testEmptyResultSet() {
        List<Payment> payments = (List<Payment>) paymentRepository.findByFromAccount("NON-EXISTENT-ACCOUNT");
        
        assertNotNull(payments);
        assertTrue(payments.isEmpty());
    }
}
