package com.researchease.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RecentProgressReportResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private String content;
    private LocalDate reportDate;
    private String creatorName;
    private LocalDateTime createdAt;
}
