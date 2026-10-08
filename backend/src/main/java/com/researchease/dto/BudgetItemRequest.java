package com.researchease.dto;

import com.researchease.enums.BudgetCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetItemRequest {
    @NotNull(message = "预算科目类别不能为空")
    private BudgetCategory category;

    @NotNull(message = "申请金额不能为空")
    @DecimalMin(value = "0.01", message = "申请金额必须大于0")
    private BigDecimal requestedAmount;
}
