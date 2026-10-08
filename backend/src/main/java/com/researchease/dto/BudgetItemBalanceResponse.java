package com.researchease.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BudgetItemBalanceResponse {
    private Long id;
    private String category;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal balance;
}
