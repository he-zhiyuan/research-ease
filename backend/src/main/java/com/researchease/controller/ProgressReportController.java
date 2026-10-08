package com.researchease.controller;

import com.researchease.common.Result;
import com.researchease.common.SecurityUtils;
import com.researchease.dto.OverdueProjectResponse;
import com.researchease.dto.ProgressReportRequest;
import com.researchease.dto.ProgressReportResponse;
import com.researchease.service.ProgressReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProgressReportController {

    private final ProgressReportService progressReportService;

    @PostMapping("/api/projects/{id}/progress-reports")
    @PreAuthorize("hasRole('RESEARCHER')")
    public Result<ProgressReportResponse> submit(@PathVariable Long id, @Valid @RequestBody ProgressReportRequest request) {
        return Result.ok(progressReportService.submit(id, request, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/api/projects/{id}/progress-reports")
    @PreAuthorize("hasAnyRole('RESEARCHER','MANAGER')")
    public Result<List<ProgressReportResponse>> listForProject(@PathVariable Long id) {
        return Result.ok(progressReportService.listForProject(id, SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentRole()));
    }

    @GetMapping("/api/progress-reports/overdue")
    @PreAuthorize("hasRole('MANAGER')")
    public Result<List<OverdueProjectResponse>> listOverdue() {
        return Result.ok(progressReportService.listOverdue());
    }
}
