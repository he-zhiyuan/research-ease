package com.researchease.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProjectUpdateRequest {
    @NotBlank(message = "项目名称不能为空")
    private String projectName;

    private String researchContent;

    private String teamMembers;

    private String fundingSource;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @NotEmpty(message = "至少需要一条预算科目")
    @Valid
    private List<BudgetItemRequest> budgetItems;
}
