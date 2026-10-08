package com.researchease.controller;

import com.researchease.common.Result;
import com.researchease.dto.DashboardOverviewResponse;
import com.researchease.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/api/dashboard/overview")
    @PreAuthorize("hasRole('MANAGER')")
    public Result<DashboardOverviewResponse> overview() {
        return Result.ok(dashboardService.getOverview());
    }
}
