package com.academicmonitor.service;

import com.academicmonitor.dto.request.StudentRequest;
import com.academicmonitor.dto.response.StudentResponse;
import com.academicmonitor.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    
    /**
     * 获取带筛选条件的分页学生列表
     * 
     * @param studentNumber 学号（可选）
     * @param name 学生姓名（可选）
     * @param college 学院（可选）
     * @param major 专业（可选）
     * @param pageable 分页信息
     * @return 分页后的学生响应对象
     */
    Page<StudentResponse> getStudentsWithFilters(String studentNumber, String name, 
                                                String college, String major, 
                                                Pageable pageable);
} 