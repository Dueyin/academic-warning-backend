package com.academicmonitor.controller;

import com.academicmonitor.dto.request.StudentRequest;
import com.academicmonitor.dto.response.MessageResponse;
import com.academicmonitor.dto.response.StudentResponse;
import com.academicmonitor.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
@RestController
@RequestMapping("/students")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    private StudentService studentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentRequest studentRequest) {
        StudentResponse createdStudent = studentService.createStudent(studentRequest);
        return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String studentNumber,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String major) {
        
        try {
            logger.info("获取学生列表 - 页码: {}, 大小: {}, 学号: {}, 姓名: {}, 学院: {}, 专业: {}", 
                page, size, studentNumber, name, college, major);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
            Page<StudentResponse> students = studentService.getStudentsWithFilters(
                studentNumber, name, college, major, pageable);
            
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            logger.error("获取学生列表失败", e);
            return ResponseEntity.badRequest().body(new MessageResponse("获取学生列表失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        StudentResponse student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    @GetMapping("/number/{studentNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<StudentResponse> getStudentByStudentNumber(@PathVariable String studentNumber) {
        StudentResponse student = studentService.getStudentByStudentNumber(studentNumber);
        return ResponseEntity.ok(student);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<StudentResponse> getStudentByUserId(@PathVariable Long userId) {
        StudentResponse student = studentService.getStudentByUserId(userId);
        return ResponseEntity.ok(student);
    }

    @GetMapping("/class/{classGroupId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<StudentResponse>> getStudentsByClassGroup(@PathVariable Long classGroupId) {
        List<StudentResponse> students = studentService.getStudentsByClassGroup(classGroupId);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/warnings")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<StudentResponse>> getStudentsWithWarnings() {
        List<StudentResponse> students = studentService.getStudentsWithWarnings();
        return ResponseEntity.ok(students);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequest studentRequest) {
        StudentResponse updatedStudent = studentService.updateStudent(id, studentRequest);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(new MessageResponse("学生信息删除成功"));
    }
} 