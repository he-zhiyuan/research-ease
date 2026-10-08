package com.researchease.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProjectResponse {
    private Long id;
    private String projectName;
    private String researchContent;
    private Long leaderId;
    private String leaderName;
    private String teamMembers;
    private String fundingSource;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BudgetItemResponse> budgetItems;
}
