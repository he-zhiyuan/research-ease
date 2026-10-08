package com.researchease.service;

import com.researchease.dto.OverdueProjectResponse;
import com.researchease.dto.ProgressReportRequest;
import com.researchease.dto.ProgressReportResponse;

import java.util.List;

public interface ProgressReportService {

    ProgressReportResponse submit(Long projectId, ProgressReportRequest request, Long currentUserId);

    List<ProgressReportResponse> listForProject(Long projectId, Long currentUserId, String currentRole);

    List<OverdueProjectResponse> listOverdue();
}
