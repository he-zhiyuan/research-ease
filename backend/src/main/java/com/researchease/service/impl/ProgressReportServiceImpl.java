package com.researchease.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.researchease.common.BusinessException;
import com.researchease.dto.OverdueProjectResponse;
import com.researchease.dto.ProgressReportRequest;
import com.researchease.dto.ProgressReportResponse;
import com.researchease.dto.RecentProgressReportResponse;
import com.researchease.entity.Project;
import com.researchease.entity.ProgressReport;
import com.researchease.entity.User;
import com.researchease.enums.ProjectStatus;
import com.researchease.enums.Role;
import com.researchease.mapper.ProjectMapper;
import com.researchease.mapper.ProgressReportMapper;
import com.researchease.mapper.UserMapper;
import com.researchease.service.ProgressReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressReportServiceImpl implements ProgressReportService {

    private final ProgressReportMapper progressReportMapper;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;

    @Value("${researchease.progress.overdue-days}")
    private long overdueDays;

    @Override
    @Transactional
    public ProgressReportResponse submit(Long projectId, ProgressReportRequest request, Long currentUserId) {
        Project project = requireProject(projectId);
        requireLeader(project, currentUserId);
        if (ProjectStatus.valueOf(project.getStatus()) != ProjectStatus.EXECUTING) {
            throw new BusinessException("仅执行中的项目可以提交进度汇报");
        }

        ProgressReport report = new ProgressReport();
        report.setProjectId(projectId);
        report.setContent(request.getContent());
        report.setReportDate(request.getReportDate());
        report.setCreatedBy(currentUserId);
        progressReportMapper.insert(report);

        return toResponse(report);
    }

    @Override
    public List<ProgressReportResponse> listForProject(Long projectId, Long currentUserId, String currentRole) {
        Project project = requireProject(projectId);
        if (Role.RESEARCHER.name().equals(currentRole)) {
            requireLeader(project, currentUserId);
        }
        List<ProgressReport> reports = progressReportMapper.selectList(
                new LambdaQueryWrapper<ProgressReport>().eq(ProgressReport::getProjectId, projectId)
                        .orderByDesc(ProgressReport::getReportDate));
        return reports.stream().map(this::toResponse).toList();
    }

    @Override
    public List<OverdueProjectResponse> listOverdue() {
        List<Project> executingProjects = projectMapper.selectList(
                new LambdaQueryWrapper<Project>().eq(Project::getStatus, ProjectStatus.EXECUTING.name()));
        LocalDate today = LocalDate.now();
        return executingProjects.stream()
                .map(project -> {
                    List<ProgressReport> reports = progressReportMapper.selectList(
                            new LambdaQueryWrapper<ProgressReport>().eq(ProgressReport::getProjectId, project.getId())
                                    .orderByDesc(ProgressReport::getReportDate));
                    LocalDate lastReportDate = reports.isEmpty()
                            ? project.getUpdatedAt().toLocalDate()
                            : reports.get(0).getReportDate();
                    long days = ChronoUnit.DAYS.between(lastReportDate, today);
                    if (days < overdueDays) {
                        return null;
                    }
                    User leader = userMapper.selectById(project.getLeaderId());
                    return new OverdueProjectResponse(project.getId(), project.getProjectName(),
                            leader != null ? leader.getRealName() : null, lastReportDate, days);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public List<RecentProgressReportResponse> listRecent(int limit) {
        List<ProgressReport> reports = progressReportMapper.selectList(
                new LambdaQueryWrapper<ProgressReport>().orderByDesc(ProgressReport::getCreatedAt));
        return reports.stream().limit(limit).map(this::toRecentResponse).toList();
    }

    private Project requireProject(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }
        return project;
    }

    private void requireLeader(Project project, Long currentUserId) {
        if (!project.getLeaderId().equals(currentUserId)) {
            throw new BusinessException(403, "仅项目负责人本人可执行该操作");
        }
    }

    private ProgressReportResponse toResponse(ProgressReport report) {
        User creator = userMapper.selectById(report.getCreatedBy());
        return new ProgressReportResponse(report.getId(), report.getProjectId(), report.getContent(),
                report.getReportDate(), report.getCreatedBy(), creator != null ? creator.getRealName() : null,
                report.getCreatedAt());
    }

    private RecentProgressReportResponse toRecentResponse(ProgressReport report) {
        User creator = userMapper.selectById(report.getCreatedBy());
        Project project = projectMapper.selectById(report.getProjectId());
        return new RecentProgressReportResponse(report.getId(), report.getProjectId(),
                project != null ? project.getProjectName() : null, report.getContent(), report.getReportDate(),
                creator != null ? creator.getRealName() : null, report.getCreatedAt());
    }
}
