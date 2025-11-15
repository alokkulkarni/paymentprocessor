package com.alok.payment.paymentprocessor.dto;

import java.math.BigDecimal;

public class AccountBalanceRequest {
    
    private String accountNumber;
    private BigDecimal amount;

    public AccountBalanceRequest() {
    }

    public AccountBalanceRequest(String accountNumber, BigDecimal amount) {
        this.accountNumber = accountNumber;
        this.amount = amount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
