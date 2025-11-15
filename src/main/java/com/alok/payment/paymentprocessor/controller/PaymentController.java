package com.alok.payment.paymentprocessor.controller;

import com.alok.payment.paymentprocessor.dto.PaymentRequest;
import com.alok.payment.paymentprocessor.dto.PaymentResponse;
import com.alok.payment.paymentprocessor.model.Payment;
import com.alok.payment.paymentprocessor.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Payment Processing
 * Provides endpoints for payment operations
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    private final PaymentService paymentService;
    
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    /**
     * Process a new payment request
     * 
     * @param request Payment request details
     * @return Payment response with transaction status
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        logger.info("Received payment request from {} to {} for amount {}", 
                   request.getFromAccount(), request.getToAccount(), request.getAmount());
        
        try {
            PaymentResponse response = paymentService.processPayment(request);
            
            // Handle null response from service
            if (response == null) {
                logger.error("Payment service returned null response");
                PaymentResponse errorResponse = new PaymentResponse();
                errorResponse.setMessage("Payment unsuccessful");
                errorResponse.setFailureReason("Invalid payment request or service error");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            if (response.getStatus().toString().contains("COMPLETED")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing payment request", e);
            
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setMessage("Payment unsuccessful");
            errorResponse.setFailureReason("Internal error: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get payment status by transaction ID
     * 
     * @param transactionId Transaction ID to look up
     * @return Payment response with current status
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable String transactionId) {
        logger.info("Retrieving payment status for transaction: {}", transactionId);
        
        return paymentService.getPaymentStatus(transactionId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all payments for a specific account
     * 
     * @param accountNumber Account number to filter by
     * @return List of payments
     */
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<Payment>> getPaymentsByAccount(@PathVariable String accountNumber) {
        logger.info("Retrieving payments for account: {}", accountNumber);
        
        List<Payment> payments = paymentService.getPaymentsByAccount(accountNumber);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Get all payments (for admin/monitoring purposes)
     * 
     * @return List of all payments
     */
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        logger.info("Retrieving all payments");
        
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Health check endpoint
     * 
     * @return Simple health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment Processor is running");
    }
}
