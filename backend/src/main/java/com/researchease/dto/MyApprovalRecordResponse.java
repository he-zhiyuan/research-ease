package com.researchease.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MyApprovalRecordResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private String type;
    private Long targetId;
    private String result;
    private String comment;
    private String operatorName;
    private LocalDateTime createdAt;
}
