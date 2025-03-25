package com.academicmonitor.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class TeacherResponse {
    private Long id;
    private Long userId;
    private String username;
    private String name;
    private String email;
    private String teacherNumber;
    private String title;
    private String specialty;
    private List<ClassGroupResponse> headOfClasses;
} 