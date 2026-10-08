package com.researchease.dto;

import com.researchease.enums.AchievementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AchievementRequest {
    @NotNull(message = "成果类型不能为空")
    private AchievementType type;

    @NotBlank(message = "成果标题不能为空")
    private String title;

    private String detail;

    private LocalDate achievedDate;
}
