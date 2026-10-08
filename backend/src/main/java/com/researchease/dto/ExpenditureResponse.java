package com.researchease.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ExpenditureResponse {
    private Long id;
    private Long projectId;
    private Long budgetItemId;
    private String budgetItemCategory;
    private BigDecimal amount;
    private String description;
    private String status;
    private Long createdBy;
    private String creatorName;
    private LocalDateTime createdAt;
}
