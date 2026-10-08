package com.researchease.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AchievementResponse {
    private Long id;
    private String type;
    private String title;
    private String detail;
    private LocalDate achievedDate;
    private LocalDateTime createdAt;
}
