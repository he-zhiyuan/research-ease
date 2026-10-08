package com.researchease.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class ApproveProjectRequest {
    @Valid
    private List<BudgetApprovalItem> budgetItems;
}
