package com.alok.payment.paymentprocessor.dto;

import com.alok.payment.paymentprocessor.model.PaymentType;

import java.math.BigDecimal;

public class PaymentRequest {
    
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String currency;
    private PaymentType paymentType;
    private String description;

    public PaymentRequest() {
    }

    public PaymentRequest(String fromAccount, String toAccount, BigDecimal amount, 
                         String currency, PaymentType paymentType, String description) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.currency = currency;
        this.paymentType = paymentType;
        this.description = description;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
