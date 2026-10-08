package com.researchease.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProgressReportRequest {
    @NotBlank(message = "汇报内容不能为空")
    private String content;

    @NotNull(message = "汇报日期不能为空")
    private LocalDate reportDate;
}
