package com.researchease.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateEnabledRequest {
    @NotNull(message = "enabled不能为空")
    private Boolean enabled;
}
