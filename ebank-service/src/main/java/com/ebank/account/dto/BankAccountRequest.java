package com.ebank.account.dto;

import com.ebank.account.enums.AccountType;

public class BankAccountRequest {

    private double balance;
    private String currency;
    private AccountType type;
    private Long customerId;

    public BankAccountRequest() {
    }

    public BankAccountRequest(double balance, String currency, AccountType type, Long customerId) {
        this.balance = balance;
        this.currency = currency;
        this.type = type;
        this.customerId = customerId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
