package com.academicmonitor.dto.response;

import lombok.Data;

@Data
public class ClassGroupResponse {
    private Long id;
    private String name;
    private String year;
    private String description;
    private Long headTeacherId;
    private String headTeacherName;
    private Integer studentCount;
} 