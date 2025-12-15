package com.alok.payment.paymentprocessor.repository;

import com.alok.payment.paymentprocessor.model.PaymentAudit;
import com.alok.payment.paymentprocessor.model.PaymentStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentAuditRepository extends CrudRepository<PaymentAudit, Long> {
    
    Optional<PaymentAudit> findByTransactionId(String transactionId);
    
    List<PaymentAudit> findByFromAccount(String fromAccount);
    
    List<PaymentAudit> findByToAccount(String toAccount);
    
    List<PaymentAudit> findByFinalStatus(PaymentStatus status);
    
    List<PaymentAudit> findByFraudCheckPassed(Boolean fraudCheckPassed);
    
    List<PaymentAudit> findByAuditedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
