package com.academicmonitor.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class TeacherRequest {
    @NotNull
    private Long userId;
    
    @NotBlank
    private String teacherNumber;
    
    private String title;
    
    private String specialty;
} 