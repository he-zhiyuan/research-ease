package com.researchease.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("expenditure")
public class Expenditure {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long budgetItemId;
    private BigDecimal amount;
    private String description;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
