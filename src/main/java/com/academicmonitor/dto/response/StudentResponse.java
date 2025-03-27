package com.academicmonitor.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentResponse {
    private Long id;
    private Long userId;
    private String username;
    private String name;
    private String email;
    private String studentNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private Long classGroupId;
    private String className;
    private String college;
    private String major;
    private Boolean hasWarning;
    private String emergencyContact;
    private String emergencyPhone;
} 