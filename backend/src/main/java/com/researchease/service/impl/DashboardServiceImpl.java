package com.researchease.service.impl;

import com.researchease.dto.DashboardOverviewResponse;
import com.researchease.enums.ExpenditureStatus;
import com.researchease.enums.ProjectStatus;
import com.researchease.mapper.DashboardMapper;
import com.researchease.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper dashboardMapper;

    @Override
    public DashboardOverviewResponse getOverview() {
        List<Map<String, Object>> rows = dashboardMapper.countProjectsByStatus();
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        long total = 0;
        for (Map<String, Object> row : rows) {
            String status = String.valueOf(row.get("status"));
            long count = ((Number) row.get("cnt")).longValue();
            statusCounts.put(status, count);
            total += count;
        }

        BigDecimal totalRequested = dashboardMapper.sumRequestedBudget();
        BigDecimal totalApproved = dashboardMapper.sumApprovedBudget();
        BigDecimal totalSpent = dashboardMapper.sumExpenditureByStatus(ExpenditureStatus.APPROVED.name());
        BigDecimal totalPending = dashboardMapper.sumExpenditureByStatus(ExpenditureStatus.PENDING.name());
        BigDecimal totalBalance = totalApproved.subtract(totalSpent).subtract(totalPending);

        long pendingProjectCount = statusCounts.getOrDefault(ProjectStatus.SUBMITTED.name(), 0L);
        long pendingAcceptanceCount = statusCounts.getOrDefault(ProjectStatus.ACCEPTING.name(), 0L);
        long pendingExpenditureCount = dashboardMapper.countExpenditureByStatus(ExpenditureStatus.PENDING.name());

        return new DashboardOverviewResponse(total, statusCounts, totalRequested, totalApproved,
                totalSpent, totalPending, totalBalance,
                pendingProjectCount, pendingAcceptanceCount, pendingExpenditureCount);
    }
}
