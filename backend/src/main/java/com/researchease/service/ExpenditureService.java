package com.researchease.service;

import com.researchease.dto.BudgetItemBalanceResponse;
import com.researchease.dto.ExpenditureRequest;
import com.researchease.dto.ExpenditureResponse;
import com.researchease.dto.RejectRequest;

import java.util.List;

public interface ExpenditureService {

    ExpenditureResponse submit(Long projectId, ExpenditureRequest request, Long currentUserId);

    List<ExpenditureResponse> listForProject(Long projectId, Long currentUserId, String currentRole);

    List<ExpenditureResponse> listByStatus(String status);

    void approve(Long expenditureId, Long operatorId);

    void reject(Long expenditureId, RejectRequest request, Long operatorId);

    List<BudgetItemBalanceResponse> listBudgetItemsWithBalance(Long projectId, Long currentUserId, String currentRole);
}
