package com.alok.payment.paymentprocessor.repository;

import com.alok.payment.paymentprocessor.model.Payment;
import com.alok.payment.paymentprocessor.model.PaymentStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends CrudRepository<Payment, Long> {
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    List<Payment> findByFromAccount(String fromAccount);
    
    List<Payment> findByToAccount(String toAccount);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    @Query("SELECT * FROM payments WHERE from_account = :account OR to_account = :account")
    List<Payment> findByAccount(@Param("account") String account);
}
