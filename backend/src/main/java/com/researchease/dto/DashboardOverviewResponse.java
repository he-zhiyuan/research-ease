package com.researchease.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
public class DashboardOverviewResponse {
    private long totalProjects;
    private Map<String, Long> statusCounts;
    private BigDecimal totalRequestedBudget;
    private BigDecimal totalApprovedBudget;
    private BigDecimal totalSpent;
    private BigDecimal totalPending;
    private BigDecimal totalBalance;
}
