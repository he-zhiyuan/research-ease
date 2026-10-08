package com.researchease.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProgressReportResponse {
    private Long id;
    private Long projectId;
    private String content;
    private LocalDate reportDate;
    private Long createdBy;
    private String creatorName;
    private LocalDateTime createdAt;
}
