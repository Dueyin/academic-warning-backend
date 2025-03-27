package com.academicmonitor.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WarningResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNumber;
    private String title;
    private String warningType;
    private Integer level;
    private String content;
    private LocalDateTime createTime;
    private String status;
    private String solution;
    private LocalDateTime processedTime;
    private Long processedById;
    private String processedByName;
} 