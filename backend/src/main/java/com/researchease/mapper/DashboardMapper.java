package com.researchease.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    List<Map<String, Object>> countProjectsByStatus();

    BigDecimal sumRequestedBudget();

    BigDecimal sumApprovedBudget();

    BigDecimal sumExpenditureByStatus(@Param("status") String status);

    long countExpenditureByStatus(@Param("status") String status);
}
