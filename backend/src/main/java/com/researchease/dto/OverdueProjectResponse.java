package com.researchease.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class OverdueProjectResponse {
    private Long projectId;
    private String projectName;
    private String leaderName;
    private LocalDate lastReportDate;
    private long daysSinceLastReport;
}
