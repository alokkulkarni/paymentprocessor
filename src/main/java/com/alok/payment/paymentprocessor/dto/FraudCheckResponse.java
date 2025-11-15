package com.alok.payment.paymentprocessor.dto;

public class FraudCheckResponse {
    
    private String transactionId;
    private boolean fraudulent;
    private String reason;
    private double riskScore;

    public FraudCheckResponse() {
    }

    public FraudCheckResponse(String transactionId, boolean fraudulent, String reason, double riskScore) {
        this.transactionId = transactionId;
        this.fraudulent = fraudulent;
        this.reason = reason;
        this.riskScore = riskScore;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public boolean isFraudulent() {
        return fraudulent;
    }

    public void setFraudulent(boolean fraudulent) {
        this.fraudulent = fraudulent;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }
}
