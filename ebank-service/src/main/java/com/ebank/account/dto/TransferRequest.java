package com.ebank.account.dto;

public class TransferRequest {

    private String accountSource;
    private String accountDestination;
    private double amount;

    public TransferRequest() {
    }

    public TransferRequest(String accountSource, String accountDestination, double amount) {
        this.accountSource = accountSource;
        this.accountDestination = accountDestination;
        this.amount = amount;
    }

    public String getAccountSource() {
        return accountSource;
    }

    public void setAccountSource(String accountSource) {
        this.accountSource = accountSource;
    }

    public String getAccountDestination() {
        return accountDestination;
    }

    public void setAccountDestination(String accountDestination) {
        this.accountDestination = accountDestination;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
