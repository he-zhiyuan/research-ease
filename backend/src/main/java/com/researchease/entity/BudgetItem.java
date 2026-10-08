package com.researchease.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("budget_item")
public class BudgetItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String category;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private LocalDateTime createdAt;
}
