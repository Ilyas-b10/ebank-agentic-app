package com.ebank.account.dto;

import com.ebank.account.enums.AccountType;
import com.ebank.account.model.Customer;

import java.util.ArrayList;
import java.util.List;

/**
 * Vue d'un compte bancaire avec son client et ses operations (utilisee par les outils MCP).
 */
public class BankAccountDto {

    private String id;
    private String createdAt;
    private double balance;
    private String currency;
    private AccountType type;
    private Long customerId;
    private Customer customer;
    private List<AccountOperationDto> operations = new ArrayList<>();

    public BankAccountDto() {
    }

    public BankAccountDto(String id, String createdAt, double balance, String currency,
                          AccountType type, Long customerId) {
        this.id = id;
        this.createdAt = createdAt;
        this.balance = balance;
        this.currency = currency;
        this.type = type;
        this.customerId = customerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<AccountOperationDto> getOperations() {
        return operations;
    }

    public void setOperations(List<AccountOperationDto> operations) {
        this.operations = operations;
    }
}
