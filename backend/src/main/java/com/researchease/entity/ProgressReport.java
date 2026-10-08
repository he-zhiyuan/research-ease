package com.researchease.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("progress_report")
public class ProgressReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String content;
    private LocalDate reportDate;
    private Long createdBy;
    private LocalDateTime createdAt;
}
