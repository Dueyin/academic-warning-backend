package com.academicmonitor.controller;

import com.academicmonitor.dto.request.StudentRequest;
import com.academicmonitor.dto.response.MessageResponse;
import com.academicmonitor.dto.response.StudentResponse;
import com.academicmonitor.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
@RestController
@RequestMapping("/api/students")
public class StudentController {

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
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        List<StudentResponse> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
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