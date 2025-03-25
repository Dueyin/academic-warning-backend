package com.academicmonitor.service;

import com.academicmonitor.dto.request.TeacherRequest;
import com.academicmonitor.dto.response.TeacherResponse;
import com.academicmonitor.entity.Teacher;

import java.util.List;

public interface TeacherService {
    
    TeacherResponse createTeacher(TeacherRequest teacherRequest);
    
    TeacherResponse getTeacherById(Long id);
    
    TeacherResponse getTeacherByTeacherNumber(String teacherNumber);
    
    TeacherResponse getTeacherByUserId(Long userId);
    
    List<TeacherResponse> getAllTeachers();
    
    TeacherResponse updateTeacher(Long id, TeacherRequest teacherRequest);
    
    void deleteTeacher(Long id);
    
    TeacherResponse mapToTeacherResponse(Teacher teacher);
} 