package com.alok.payment.paymentprocessor.dto;

import java.math.BigDecimal;

public class AccountBalanceResponse {
    
    private String accountNumber;
    private boolean valid;
    private boolean sufficientBalance;
    private BigDecimal availableBalance;
    private String message;

    public AccountBalanceResponse() {
    }

    public AccountBalanceResponse(String accountNumber, boolean valid, boolean sufficientBalance, 
                                 BigDecimal availableBalance, String message) {
        this.accountNumber = accountNumber;
        this.valid = valid;
        this.sufficientBalance = sufficientBalance;
        this.availableBalance = availableBalance;
        this.message = message;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isSufficientBalance() {
        return sufficientBalance;
    }

    public void setSufficientBalance(boolean sufficientBalance) {
        this.sufficientBalance = sufficientBalance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
