package com.academicmonitor.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class StudentRequest {
    @NotNull
    private Long userId;
    
    @NotBlank
    private String studentNumber;
    
    private LocalDate dateOfBirth;
    
    private String gender;
    
    private String address;
    
    private Long classGroupId;
    
    private String emergencyContact;
    
    private String emergencyPhone;
} 