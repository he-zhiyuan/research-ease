package com.researchease.service;

import com.researchease.dto.ApprovalRecordResponse;
import com.researchease.dto.ApproveProjectRequest;
import com.researchease.dto.ProjectCreateRequest;
import com.researchease.dto.ProjectResponse;
import com.researchease.dto.ProjectUpdateRequest;
import com.researchease.dto.RejectRequest;

import java.util.List;

public interface ProjectService {

    ProjectResponse create(ProjectCreateRequest request, Long leaderId);

    void update(Long projectId, ProjectUpdateRequest request, Long currentUserId);

    void submit(Long projectId, Long currentUserId);

    void approve(Long projectId, ApproveProjectRequest request, Long operatorId);

    void reject(Long projectId, RejectRequest request, Long operatorId);

    ProjectResponse getById(Long projectId, Long currentUserId, String currentRole);

    List<ProjectResponse> listMy(Long currentUserId);

    List<ProjectResponse> listForManager(String status, String keyword);

    List<ApprovalRecordResponse> listApprovalRecords(Long projectId, Long currentUserId, String currentRole);
}
