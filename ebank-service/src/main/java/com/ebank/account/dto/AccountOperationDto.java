package com.ebank.account.dto;

import com.ebank.account.enums.OperationType;

/**
 * Vue d'une operation sans reference circulaire ni collection paresseuse
 * (utilisee par les outils MCP).
 */
public class AccountOperationDto {

    private Long id;
    private String operationDate;
    private double amount;
    private OperationType type;
    private String description;

    public AccountOperationDto() {
    }

    public AccountOperationDto(Long id, String operationDate, double amount,
                               OperationType type, String description) {
        this.id = id;
        this.operationDate = operationDate;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(String operationDate) {
        this.operationDate = operationDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
