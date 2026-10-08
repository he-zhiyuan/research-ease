package com.researchease.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AcceptanceRequest {
    @NotEmpty(message = "结题申请必须包含至少一项成果")
    @Valid
    private List<AchievementRequest> achievements;
}
