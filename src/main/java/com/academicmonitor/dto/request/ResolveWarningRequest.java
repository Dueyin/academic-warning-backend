package com.academicmonitor.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ResolveWarningRequest {
    
    @NotBlank(message = "解决方案不能为空")
    private String solution;
} 