package com.alok.payment.paymentprocessor.repository;

import com.alok.payment.paymentprocessor.model.PaymentAudit;
import com.alok.payment.paymentprocessor.model.PaymentStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for PaymentAudit entities
 * Provides data access methods for payment audit records
 */
@Repository
public interface PaymentAuditRepository extends CrudRepository<PaymentAudit, Long> {
    
    List<PaymentAudit> findByTransactionId(String transactionId);
    
    List<PaymentAudit> findByAction(String action);
    
    List<PaymentAudit> findByNewStatus(PaymentStatus status);
    
    @Query("SELECT * FROM payment_audit WHERE from_account = :account OR to_account = :account ORDER BY audit_timestamp DESC")
    List<PaymentAudit> findByAccount(@Param("account") String account);
    
    @Query("SELECT * FROM payment_audit WHERE audit_timestamp BETWEEN :startDate AND :endDate ORDER BY audit_timestamp DESC")
    List<PaymentAudit> findByAuditTimestampBetween(@Param("startDate") LocalDateTime startDate, 
                                                    @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT * FROM payment_audit ORDER BY audit_timestamp DESC LIMIT :limit")
    List<PaymentAudit> findRecentAudits(@Param("limit") int limit);
}
