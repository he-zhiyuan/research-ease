package com.researchease.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetApprovalItem {
    @NotNull(message = "预算科目ID不能为空")
    private Long budgetItemId;

    @NotNull(message = "核定金额不能为空")
    @DecimalMin(value = "0", message = "核定金额不能为负")
    private BigDecimal approvedAmount;
}
