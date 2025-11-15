package com.alok.payment.paymentprocessor.service;

import com.alok.payment.paymentprocessor.dto.*;
import com.alok.payment.paymentprocessor.model.Payment;
import com.alok.payment.paymentprocessor.model.PaymentStatus;
import com.alok.payment.paymentprocessor.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment Processing Service
 * Orchestrates payment processing with fraud detection and balance validation
 */
@Service
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    private final PaymentRepository paymentRepository;
    private final FraudService fraudService;
    private final AccountService accountService;
    
    public PaymentService(PaymentRepository paymentRepository, 
                         FraudService fraudService,
                         AccountService accountService) {
        this.paymentRepository = paymentRepository;
        this.fraudService = fraudService;
        this.accountService = accountService;
    }
    
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        logger.info("Processing payment from {} to {} for amount {}", 
                   request.getFromAccount(), request.getToAccount(), request.getAmount());
        
        // Generate transaction ID
        String transactionId = UUID.randomUUID().toString();
        
        // Create payment entity
        Payment payment = new Payment(
            transactionId,
            request.getFromAccount(),
            request.getToAccount(),
            request.getAmount(),
            request.getCurrency(),
            request.getPaymentType(),
            request.getDescription()
        );
        
        // Save initial payment record
        payment = paymentRepository.save(payment);
        
        try {
            // Step 1: Validate source account
            logger.info("Step 1: Validating source account {}", request.getFromAccount());
            AccountBalanceResponse sourceAccountValidation = accountService.validateAccount(request.getFromAccount());
            if (!sourceAccountValidation.isValid()) {
                return handlePaymentFailure(payment, PaymentStatus.ACCOUNT_VALIDATION_FAILED, 
                    "Source account validation failed: " + sourceAccountValidation.getMessage());
            }
            
            // Step 2: Validate destination account
            logger.info("Step 2: Validating destination account {}", request.getToAccount());
            AccountBalanceResponse destAccountValidation = accountService.validateAccount(request.getToAccount());
            if (!destAccountValidation.isValid()) {
                return handlePaymentFailure(payment, PaymentStatus.ACCOUNT_VALIDATION_FAILED, 
                    "Destination account validation failed: " + destAccountValidation.getMessage());
            }
            
            // Step 3: Fraud check
            logger.info("Step 3: Performing fraud check");
            FraudCheckRequest fraudRequest = new FraudCheckRequest(
                transactionId,
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount(),
                request.getCurrency()
            );
            FraudCheckResponse fraudCheck = fraudService.checkFraud(fraudRequest);
            
            if (fraudCheck.isFraudulent()) {
                return handlePaymentFailure(payment, PaymentStatus.FRAUD_CHECK_FAILED, 
                    "Fraud detected: " + fraudCheck.getReason());
            }
            
            // Step 4: Check balance
            logger.info("Step 4: Checking account balance");
            AccountBalanceRequest balanceRequest = new AccountBalanceRequest(
                request.getFromAccount(),
                request.getAmount()
            );
            AccountBalanceResponse balanceCheck = accountService.checkBalance(balanceRequest);
            
            if (!balanceCheck.isSufficientBalance()) {
                return handlePaymentFailure(payment, PaymentStatus.INSUFFICIENT_BALANCE, 
                    balanceCheck.getMessage());
            }
            
            // Step 5: Process payment
            logger.info("Step 5: Processing payment");
            payment.setStatus(PaymentStatus.PROCESSING);
            paymentRepository.save(payment);
            
            // Deduct from source account and credit to destination account
            accountService.deductBalance(request.getFromAccount(), request.getAmount());
            accountService.addBalance(request.getToAccount(), request.getAmount());
            
            // Step 6: Complete payment
            payment.setStatus(PaymentStatus.COMPLETED);
            payment = paymentRepository.save(payment);
            
            logger.info("Payment completed successfully: {}", transactionId);
            
            return buildSuccessResponse(payment);
            
        } catch (Exception e) {
            logger.error("Error processing payment: {}", transactionId, e);
            return handlePaymentFailure(payment, PaymentStatus.FAILED, 
                "Payment processing failed: " + e.getMessage());
        }
    }
    
    private PaymentResponse handlePaymentFailure(Payment payment, PaymentStatus status, String reason) {
        logger.warn("Payment failed - Transaction: {}, Status: {}, Reason: {}", 
                   payment.getTransactionId(), status, reason);
        
        payment.setStatus(status);
        payment.setFailureReason(reason);
        payment = paymentRepository.save(payment);
        
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(payment.getTransactionId());
        response.setFromAccount(payment.getFromAccount());
        response.setToAccount(payment.getToAccount());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setPaymentType(payment.getPaymentType());
        response.setStatus(status);
        response.setMessage("Payment unsuccessful");
        response.setFailureReason(reason);
        
        return response;
    }
    
    private PaymentResponse buildSuccessResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(payment.getTransactionId());
        response.setFromAccount(payment.getFromAccount());
        response.setToAccount(payment.getToAccount());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setPaymentType(payment.getPaymentType());
        response.setStatus(PaymentStatus.COMPLETED);
        response.setMessage("Payment successful");
        
        return response;
    }
    
    public Optional<PaymentResponse> getPaymentStatus(String transactionId) {
        logger.info("Retrieving payment status for transaction: {}", transactionId);
        
        return paymentRepository.findByTransactionId(transactionId)
            .map(payment -> {
                PaymentResponse response = new PaymentResponse();
                response.setTransactionId(payment.getTransactionId());
                response.setFromAccount(payment.getFromAccount());
                response.setToAccount(payment.getToAccount());
                response.setAmount(payment.getAmount());
                response.setCurrency(payment.getCurrency());
                response.setPaymentType(payment.getPaymentType());
                response.setStatus(payment.getStatus());
                response.setTimestamp(payment.getUpdatedAt());
                
                if (payment.getStatus() == PaymentStatus.COMPLETED) {
                    response.setMessage("Payment successful");
                } else {
                    response.setMessage("Payment unsuccessful");
                    response.setFailureReason(payment.getFailureReason());
                }
                
                return response;
            });
    }
    
    public List<Payment> getPaymentsByAccount(String accountNumber) {
        logger.info("Retrieving payments for account: {}", accountNumber);
        return (List<Payment>) paymentRepository.findByAccount(accountNumber);
    }
    
    public List<Payment> getAllPayments() {
        logger.info("Retrieving all payments");
        return (List<Payment>) paymentRepository.findAll();
    }
}
