package com.academicmonitor.service.impl;

import com.academicmonitor.dto.request.StudentRequest;
import com.academicmonitor.dto.response.StudentResponse;
import com.academicmonitor.entity.ClassGroup;
import com.academicmonitor.entity.Student;
import com.academicmonitor.entity.User;
import com.academicmonitor.exception.ResourceNotFoundException;
import com.academicmonitor.repository.ClassGroupRepository;
import com.academicmonitor.repository.StudentRepository;
import com.academicmonitor.repository.UserRepository;
import com.academicmonitor.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassGroupRepository classGroupRepository;

    @Override
    @Transactional
    public StudentResponse createStudent(StudentRequest studentRequest) {
        // 获取用户
        User user = userRepository.findById(studentRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + studentRequest.getUserId()));

        // 检查学号是否已存在
        if (studentRepository.existsByStudentNumber(studentRequest.getStudentNumber())) {
            throw new RuntimeException("学号已存在: " + studentRequest.getStudentNumber());
        }

        // 创建学生对象
        Student student = new Student();
        student.setUser(user);
        student.setStudentNumber(studentRequest.getStudentNumber());
        student.setDateOfBirth(studentRequest.getDateOfBirth());
        student.setGender(studentRequest.getGender());
        student.setAddress(studentRequest.getAddress());
        student.setEmergencyContact(studentRequest.getEmergencyContact());
        student.setEmergencyPhone(studentRequest.getEmergencyPhone());
        student.setHasWarning(false);

        // 设置班级（如果有）
        if (studentRequest.getClassGroupId() != null) {
            ClassGroup classGroup = classGroupRepository.findById(studentRequest.getClassGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("班级不存在，ID: " + studentRequest.getClassGroupId()));
            student.setClassGroup(classGroup);
        }

        Student savedStudent = studentRepository.save(student);
        return mapToStudentResponse(savedStudent);
    }

    @Override
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在，ID: " + id));
        return mapToStudentResponse(student);
    }

    @Override
    public StudentResponse getStudentByStudentNumber(String studentNumber) {
        Student student = studentRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在，学号: " + studentNumber));
        return mapToStudentResponse(student);
    }

    @Override
    public StudentResponse getStudentByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + userId));
        
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("该用户没有关联的学生信息，用户ID: " + userId));
        
        return mapToStudentResponse(student);
    }

    @Override
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapToStudentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentResponse> getStudentsByClassGroup(Long classGroupId) {
        ClassGroup classGroup = classGroupRepository.findById(classGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("班级不存在，ID: " + classGroupId));
        
        return studentRepository.findByClassGroup(classGroup).stream()
                .map(this::mapToStudentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentResponse> getStudentsWithWarnings() {
        return studentRepository.findAllWithWarnings().stream()
                .map(this::mapToStudentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<StudentResponse> getStudentsWithFilters(String studentNumber, String name,
                                                     String college, String major,
                                                     Pageable pageable) {
        // 使用自定义查询方法获取分页结果
        Page<Student> studentsPage = studentRepository.findByFilters(
                studentNumber, name, college, major, pageable);
        
        // 将每个Student实体转换为StudentResponse
        return studentsPage.map(this::mapToStudentResponse);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest studentRequest) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在，ID: " + id));
        
        // 更新学生信息
        if (studentRequest.getStudentNumber() != null && 
                !student.getStudentNumber().equals(studentRequest.getStudentNumber())) {
            // 检查学号是否已被其他学生使用
            if (studentRepository.existsByStudentNumber(studentRequest.getStudentNumber())) {
                throw new RuntimeException("学号已存在: " + studentRequest.getStudentNumber());
            }
            student.setStudentNumber(studentRequest.getStudentNumber());
        }
        
        student.setDateOfBirth(studentRequest.getDateOfBirth());
        student.setGender(studentRequest.getGender());
        student.setAddress(studentRequest.getAddress());
        student.setEmergencyContact(studentRequest.getEmergencyContact());
        student.setEmergencyPhone(studentRequest.getEmergencyPhone());
        
        // 更新班级（如果有）
        if (studentRequest.getClassGroupId() != null) {
            ClassGroup classGroup = classGroupRepository.findById(studentRequest.getClassGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("班级不存在，ID: " + studentRequest.getClassGroupId()));
            student.setClassGroup(classGroup);
        } else {
            student.setClassGroup(null);
        }
        
        Student updatedStudent = studentRepository.save(student);
        return mapToStudentResponse(updatedStudent);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在，ID: " + id));
        
        studentRepository.delete(student);
    }

    @Override
    public StudentResponse mapToStudentResponse(Student student) {
        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setUserId(student.getUser().getId());
        response.setUsername(student.getUser().getUsername());
        response.setName(student.getUser().getName());
        response.setEmail(student.getUser().getEmail());
        response.setStudentNumber(student.getStudentNumber());
        response.setDateOfBirth(student.getDateOfBirth());
        response.setGender(student.getGender());
        response.setAddress(student.getAddress());
        response.setHasWarning(student.getHasWarning());
        response.setEmergencyContact(student.getEmergencyContact());
        response.setEmergencyPhone(student.getEmergencyPhone());
        
        if (student.getClassGroup() != null) {
            response.setClassGroupId(student.getClassGroup().getId());
            response.setClassName(student.getClassGroup().getName());
            response.setCollege(student.getClassGroup().getCollege());
            response.setMajor(student.getClassGroup().getMajor());
        } else {
            response.setClassGroupId(null);
            response.setClassName("未分配班级");
            response.setCollege("");
            response.setMajor("");
        }
        
        return response;
    }
} 