package com.academicmonitor.service;

import com.academicmonitor.dto.request.StudentRequest;
import com.academicmonitor.dto.response.StudentResponse;
import com.academicmonitor.entity.Student;

import java.util.List;

public interface StudentService {
    
    StudentResponse createStudent(StudentRequest studentRequest);
    
    StudentResponse getStudentById(Long id);
    
    StudentResponse getStudentByStudentNumber(String studentNumber);
    
    StudentResponse getStudentByUserId(Long userId);
    
    List<StudentResponse> getAllStudents();
    
    List<StudentResponse> getStudentsByClassGroup(Long classGroupId);
    
    List<StudentResponse> getStudentsWithWarnings();
    
    StudentResponse updateStudent(Long id, StudentRequest studentRequest);
    
    void deleteStudent(Long id);
    
    StudentResponse mapToStudentResponse(Student student);
} 