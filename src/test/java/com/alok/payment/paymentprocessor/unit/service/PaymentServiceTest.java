package com.alok.payment.paymentprocessor.unit.service;

import com.alok.payment.paymentprocessor.dto.AccountBalanceResponse;
import com.alok.payment.paymentprocessor.dto.FraudCheckResponse;
import com.alok.payment.paymentprocessor.dto.PaymentRequest;
import com.alok.payment.paymentprocessor.dto.PaymentResponse;
import com.alok.payment.paymentprocessor.model.Payment;
import com.alok.payment.paymentprocessor.model.PaymentAudit;
import com.alok.payment.paymentprocessor.model.PaymentStatus;
import com.alok.payment.paymentprocessor.model.PaymentType;
import com.alok.payment.paymentprocessor.repository.PaymentRepository;
import com.alok.payment.paymentprocessor.service.AccountService;
import com.alok.payment.paymentprocessor.service.FraudService;
import com.alok.payment.paymentprocessor.service.PaymentAuditService;
import com.alok.payment.paymentprocessor.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FraudService fraudService;

    @Mock
    private AccountService accountService;

    @Mock
    private PaymentAuditService auditService;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest validPaymentRequest;
    private Payment savedPayment;

    @BeforeEach
    void setUp() {
        validPaymentRequest = new PaymentRequest();
        validPaymentRequest.setFromAccount("ACC001");
        validPaymentRequest.setToAccount("ACC002");
        validPaymentRequest.setAmount(new BigDecimal("1000.00"));
        validPaymentRequest.setCurrency("USD");
        validPaymentRequest.setPaymentType(PaymentType.DOMESTIC_TRANSFER);
        validPaymentRequest.setDescription("Test payment");

        savedPayment = new Payment();
        savedPayment.setId(1L);
        savedPayment.setTransactionId("TXN-001");
        savedPayment.setFromAccount("ACC001");
        savedPayment.setToAccount("ACC002");
        savedPayment.setAmount(new BigDecimal("1000.00"));
        savedPayment.setCurrency("USD");
        savedPayment.setPaymentType(PaymentType.DOMESTIC_TRANSFER);
        savedPayment.setStatus(PaymentStatus.PENDING);
        savedPayment.setDescription("Test payment");
    }

    @Test
    @DisplayName("Should process payment successfully")
    void testSuccessfulPaymentProcessing() {
        // Mock fraud check - pass
        FraudCheckResponse fraudResponse = new FraudCheckResponse();
        fraudResponse.setFraudulent(false);
        fraudResponse.setRiskScore(0.2);
        when(fraudService.checkFraud(any())).thenReturn(fraudResponse);

        // Mock source account validation - valid
        AccountBalanceResponse sourceResponse = new AccountBalanceResponse();
        sourceResponse.setValid(true);
        sourceResponse.setAccountNumber("ACC001");
        sourceResponse.setAvailableBalance(new BigDecimal("10000.00"));
        when(accountService.validateAccount("ACC001")).thenReturn(sourceResponse);

        // Mock destination account validation - valid
        AccountBalanceResponse destResponse = new AccountBalanceResponse();
        destResponse.setValid(true);
        destResponse.setAccountNumber("ACC002");
        destResponse.setAvailableBalance(new BigDecimal("5000.00"));
        when(accountService.validateAccount("ACC002")).thenReturn(destResponse);

        // Mock balance check - sufficient
        AccountBalanceResponse balanceResponse = new AccountBalanceResponse();
        balanceResponse.setValid(true);
        balanceResponse.setSufficientBalance(true);
        when(accountService.checkBalance(any())).thenReturn(balanceResponse);

        // Mock repository save
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        
        // Mock audit service
        when(auditService.logPaymentCreated(any(Payment.class), anyString())).thenReturn(new PaymentAudit());
        when(auditService.logStatusChange(any(Payment.class), any(PaymentStatus.class), anyString())).thenReturn(new PaymentAudit());
        when(auditService.logPaymentCompleted(any(Payment.class), anyString())).thenReturn(new PaymentAudit());

        PaymentResponse response = paymentService.processPayment(validPaymentRequest);

        assertNotNull(response);
        assertEquals(PaymentStatus.COMPLETED, response.getStatus());
        assertNotNull(response.getTransactionId());
        verify(accountService, times(1)).deductBalance(eq("ACC001"), any(BigDecimal.class));
        verify(accountService, times(1)).addBalance(eq("ACC002"), any(BigDecimal.class));
        verify(paymentRepository, atLeast(2)).save(any(Payment.class));
        verify(auditService, times(1)).logPaymentCreated(any(Payment.class), anyString());
        verify(auditService, times(1)).logPaymentCompleted(any(Payment.class), anyString());
    }

    @Test
    @DisplayName("Should fail payment when fraud detected")
    void testPaymentFailsOnFraudDetection() {
        // Mock fraud check - fraud detected
        FraudCheckResponse fraudResponse = new FraudCheckResponse();
        fraudResponse.setFraudulent(true);
        fraudResponse.setReason("High risk transaction");
        when(fraudService.checkFraud(any())).thenReturn(fraudResponse);

        // Mock source account validation - valid
        AccountBalanceResponse sourceResponse = new AccountBalanceResponse();
        sourceResponse.setValid(true);
        when(accountService.validateAccount("ACC001")).thenReturn(sourceResponse);

        // Mock destination account validation - valid
        AccountBalanceResponse destResponse = new AccountBalanceResponse();
        destResponse.setValid(true);
        when(accountService.validateAccount("ACC002")).thenReturn(destResponse);

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        
        // Mock audit service
        when(auditService.logPaymentCreated(any(Payment.class), anyString())).thenReturn(new PaymentAudit());
        when(auditService.logPaymentFailed(any(Payment.class), any(PaymentStatus.class), anyString(), anyString())).thenReturn(new PaymentAudit());

        PaymentResponse response = paymentService.processPayment(validPaymentRequest);

        assertEquals(PaymentStatus.FRAUD_CHECK_FAILED, response.getStatus());
        assertNotNull(response.getFailureReason());
        assertTrue(response.getFailureReason().toLowerCase().contains("fraud"));
        verify(accountService, never()).deductBalance(anyString(), any(BigDecimal.class));
        verify(accountService, never()).addBalance(anyString(), any(BigDecimal.class));
        verify(auditService, times(1)).logPaymentFailed(any(Payment.class), any(PaymentStatus.class), anyString(), anyString());
    }

    @Test
    @DisplayName("Should fail payment when insufficient balance")
    void testPaymentFailsOnInsufficientBalance() {
        // Mock fraud check - pass
        FraudCheckResponse fraudResponse = new FraudCheckResponse();
        fraudResponse.setFraudulent(false);
        when(fraudService.checkFraud(any())).thenReturn(fraudResponse);

        // Mock source account validation - valid
        AccountBalanceResponse sourceResponse = new AccountBalanceResponse();
        sourceResponse.setValid(true);
        when(accountService.validateAccount("ACC001")).thenReturn(sourceResponse);

        // Mock destination account validation - valid
        AccountBalanceResponse destResponse = new AccountBalanceResponse();
        destResponse.setValid(true);
        when(accountService.validateAccount("ACC002")).thenReturn(destResponse);

        // Mock balance check - insufficient
        AccountBalanceResponse balanceResponse = new AccountBalanceResponse();
        balanceResponse.setValid(true);
        balanceResponse.setSufficientBalance(false);
        balanceResponse.setMessage("Insufficient balance");
        when(accountService.checkBalance(any())).thenReturn(balanceResponse);

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        
        // Mock audit service
        when(auditService.logPaymentCreated(any(Payment.class), anyString())).thenReturn(new PaymentAudit());
        when(auditService.logPaymentFailed(any(Payment.class), any(PaymentStatus.class), anyString(), anyString())).thenReturn(new PaymentAudit());

        PaymentResponse response = paymentService.processPayment(validPaymentRequest);

        assertEquals(PaymentStatus.INSUFFICIENT_BALANCE, response.getStatus());
        assertNotNull(response.getFailureReason());
        verify(accountService, never()).deductBalance(anyString(), any(BigDecimal.class));
        verify(accountService, never()).addBalance(anyString(), any(BigDecimal.class));
        verify(auditService, times(1)).logPaymentFailed(any(Payment.class), any(PaymentStatus.class), anyString(), anyString());
    }

    @Test
    @DisplayName("Should fail payment when source account invalid")
    void testPaymentFailsOnInvalidSourceAccount() {
        // Mock source account validation - invalid
        AccountBalanceResponse sourceResponse = new AccountBalanceResponse();
        sourceResponse.setValid(false);
        sourceResponse.setMessage("Account not found");
        when(accountService.validateAccount("ACC001")).thenReturn(sourceResponse);

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        
        // Mock audit service
        when(auditService.logPaymentCreated(any(Payment.class), anyString())).thenReturn(new PaymentAudit());
        when(auditService.logPaymentFailed(any(Payment.class), any(PaymentStatus.class), anyString(), anyString())).thenReturn(new PaymentAudit());

        PaymentResponse response = paymentService.processPayment(validPaymentRequest);

        assertEquals(PaymentStatus.ACCOUNT_VALIDATION_FAILED, response.getStatus());
        assertNotNull(response.getFailureReason());
        verify(fraudService, never()).checkFraud(any());
        verify(accountService, never()).deductBalance(anyString(), any(BigDecimal.class));
        verify(auditService, times(1)).logPaymentFailed(any(Payment.class), any(PaymentStatus.class), anyString(), anyString());
    }

    @Test
    @DisplayName("Should fail payment when destination account invalid")
    void testPaymentFailsOnInvalidDestinationAccount() {
        // Mock source account validation - valid
        AccountBalanceResponse sourceResponse = new AccountBalanceResponse();
        sourceResponse.setValid(true);
        when(accountService.validateAccount("ACC001")).thenReturn(sourceResponse);

        // Mock destination account validation - invalid
        AccountBalanceResponse destResponse = new AccountBalanceResponse();
        destResponse.setValid(false);
        destResponse.setMessage("Account not found");
        when(accountService.validateAccount("ACC002")).thenReturn(destResponse);

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        
        // Mock audit service
        when(auditService.logPaymentCreated(any(Payment.class), anyString())).thenReturn(new PaymentAudit());
        when(auditService.logPaymentFailed(any(Payment.class), any(PaymentStatus.class), anyString(), anyString())).thenReturn(new PaymentAudit());

        PaymentResponse response = paymentService.processPayment(validPaymentRequest);

        assertEquals(PaymentStatus.ACCOUNT_VALIDATION_FAILED, response.getStatus());
        assertNotNull(response.getFailureReason());
        verify(fraudService, never()).checkFraud(any());
        verify(auditService, times(1)).logPaymentFailed(any(Payment.class), any(PaymentStatus.class), anyString(), anyString());
    }

    @Test
    @DisplayName("Should retrieve payment by transaction ID")
    void testGetPaymentByTransactionId() {
        savedPayment.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findByTransactionId("TXN-001")).thenReturn(Optional.of(savedPayment));

        Optional<PaymentResponse> payment = paymentService.getPaymentStatus("TXN-001");

        assertTrue(payment.isPresent());
        assertEquals("TXN-001", payment.get().getTransactionId());
        assertEquals(PaymentStatus.COMPLETED, payment.get().getStatus());
        verify(paymentRepository, times(1)).findByTransactionId("TXN-001");
    }

    @Test
    @DisplayName("Should return empty when payment not found")
    void testGetPaymentByTransactionIdNotFound() {
        when(paymentRepository.findByTransactionId("INVALID")).thenReturn(Optional.empty());

        Optional<PaymentResponse> payment = paymentService.getPaymentStatus("INVALID");

        assertFalse(payment.isPresent());
    }

    @Test
    @DisplayName("Should retrieve payments by account number")
    void testGetPaymentsByAccount() {
        List<Payment> payments = new ArrayList<>();
        payments.add(savedPayment);

        when(paymentRepository.findByAccount("ACC001")).thenReturn(payments);

        List<Payment> result = paymentService.getPaymentsByAccount("ACC001");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository, times(1)).findByAccount("ACC001");
    }

    @Test
    @DisplayName("Should retrieve all payments")
    void testGetAllPayments() {
        List<Payment> payments = new ArrayList<>();
        payments.add(savedPayment);

        when(paymentRepository.findAll()).thenReturn(payments);

        List<Payment> result = paymentService.getAllPayments();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should handle exception during payment processing")
    void testPaymentProcessingWithException() {
        // Mock repository to throw exception on save
        when(paymentRepository.save(any(Payment.class)))
            .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            paymentService.processPayment(validPaymentRequest);
        });
    }
}
