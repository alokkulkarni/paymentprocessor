package com.alok.payment.paymentprocessor.unit.controller;

import com.alok.payment.paymentprocessor.controller.PaymentController;
import com.alok.payment.paymentprocessor.dto.PaymentRequest;
import com.alok.payment.paymentprocessor.dto.PaymentResponse;
import com.alok.payment.paymentprocessor.model.Payment;
import com.alok.payment.paymentprocessor.model.PaymentStatus;
import com.alok.payment.paymentprocessor.model.PaymentType;
import com.alok.payment.paymentprocessor.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(PaymentController.class)
@DisplayName("PaymentController Unit Tests")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private PaymentRequest validPaymentRequest;
    private PaymentResponse successResponse;
    private Payment payment;

    @BeforeEach
    void setUp() {
        validPaymentRequest = new PaymentRequest();
        validPaymentRequest.setFromAccount("ACC001");
        validPaymentRequest.setToAccount("ACC002");
        validPaymentRequest.setAmount(new BigDecimal("1000.00"));
        validPaymentRequest.setCurrency("USD");
        validPaymentRequest.setPaymentType(PaymentType.DOMESTIC_TRANSFER);
        validPaymentRequest.setDescription("Test payment");

        successResponse = new PaymentResponse();
        successResponse.setTransactionId("TXN-001");
        successResponse.setFromAccount("ACC001");
        successResponse.setToAccount("ACC002");
        successResponse.setAmount(new BigDecimal("1000.00"));
        successResponse.setCurrency("USD");
        successResponse.setPaymentType(PaymentType.DOMESTIC_TRANSFER);
        successResponse.setStatus(PaymentStatus.COMPLETED);
        successResponse.setMessage("Payment successful");

        payment = new Payment();
        payment.setId(1L);
        payment.setTransactionId("TXN-001");
        payment.setFromAccount("ACC001");
        payment.setToAccount("ACC002");
        payment.setAmount(new BigDecimal("1000.00"));
        payment.setCurrency("USD");
        payment.setPaymentType(PaymentType.DOMESTIC_TRANSFER);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setDescription("Test payment");
    }

    @Test
    @DisplayName("Should process payment successfully via API")
    void testProcessPaymentSuccess() throws Exception {
        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(successResponse);

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-001"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.message").value("Payment successful"))
                .andExpect(jsonPath("$.amount").value(1000.00));
    }

    @Test
    @DisplayName("Should handle fraud failure via API")
    void testProcessPaymentFraudFailure() throws Exception {
        PaymentResponse fraudResponse = new PaymentResponse();
        fraudResponse.setTransactionId("TXN-002");
        fraudResponse.setStatus(PaymentStatus.FRAUD_CHECK_FAILED);
        fraudResponse.setMessage("Payment unsuccessful");
        fraudResponse.setFailureReason("Fraud detected: High risk transaction");

        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(fraudResponse);

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.transactionId").value("TXN-002"))
                .andExpect(jsonPath("$.status").value("FRAUD_CHECK_FAILED"))
                .andExpect(jsonPath("$.failureReason").exists());
    }

    @Test
    @DisplayName("Should handle insufficient balance via API")
    void testProcessPaymentInsufficientBalance() throws Exception {
        PaymentResponse insufficientResponse = new PaymentResponse();
        insufficientResponse.setTransactionId("TXN-003");
        insufficientResponse.setStatus(PaymentStatus.INSUFFICIENT_BALANCE);
        insufficientResponse.setMessage("Payment unsuccessful");
        insufficientResponse.setFailureReason("Insufficient balance");

        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(insufficientResponse);

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.transactionId").value("TXN-003"))
                .andExpect(jsonPath("$.status").value("INSUFFICIENT_BALANCE"))
                .andExpect(jsonPath("$.failureReason").value("Insufficient balance"));
    }

    @Test
    @DisplayName("Should get payment status by transaction ID")
    void testGetPaymentStatusSuccess() throws Exception {
        when(paymentService.getPaymentStatus("TXN-001")).thenReturn(Optional.of(successResponse));

        mockMvc.perform(get("/api/payments/TXN-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-001"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Should return 404 when payment not found")
    void testGetPaymentStatusNotFound() throws Exception {
        when(paymentService.getPaymentStatus("INVALID")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/payments/INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get payments by account number")
    void testGetPaymentsByAccount() throws Exception {
        List<Payment> payments = new ArrayList<>();
        payments.add(payment);

        when(paymentService.getPaymentsByAccount("ACC001")).thenReturn(payments);

        mockMvc.perform(get("/api/payments/account/ACC001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].transactionId").value("TXN-001"))
                .andExpect(jsonPath("$[0].fromAccount").value("ACC001"));
    }

    @Test
    @DisplayName("Should get all payments")
    void testGetAllPayments() throws Exception {
        List<Payment> payments = new ArrayList<>();
        payments.add(payment);

        when(paymentService.getAllPayments()).thenReturn(payments);

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].transactionId").value("TXN-001"));
    }

    @Test
    @DisplayName("Should check health status")
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/payments/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment Processor is running"));
    }

    @Test
    @DisplayName("Should return empty list when no payments for account")
    void testGetPaymentsByAccountEmpty() throws Exception {
        when(paymentService.getPaymentsByAccount(anyString())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/payments/account/ACC999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Should validate request body fields")
    void testPaymentRequestValidation() throws Exception {
        PaymentRequest invalidRequest = new PaymentRequest();
        // Missing required fields - mock service to return null
        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(null);

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Payment unsuccessful"))
                .andExpect(jsonPath("$.failureReason").value("Invalid payment request or service error"));
    }
}
