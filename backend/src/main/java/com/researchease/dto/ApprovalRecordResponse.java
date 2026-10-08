package com.researchease.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApprovalRecordResponse {
    private Long id;
    private String type;
    private Long targetId;
    private String result;
    private String comment;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createdAt;
}
