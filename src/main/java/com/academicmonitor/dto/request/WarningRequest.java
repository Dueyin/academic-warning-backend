package com.academicmonitor.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class WarningRequest {
    
    @NotNull(message = "学生ID不能为空")
    private Long studentId;
    
    @NotNull(message = "预警规则ID不能为空")
    private Long ruleId;
    
    @NotBlank(message = "预警内容不能为空")
    private String content;
} 