package com.academicmonitor.controller;

import com.academicmonitor.dto.request.ResolveWarningRequest;
import com.academicmonitor.dto.request.WarningRequest;
import com.academicmonitor.dto.response.MessageResponse;
import com.academicmonitor.dto.response.WarningResponse;
import com.academicmonitor.dto.response.WarningTypeResponse;
import com.academicmonitor.entity.Student;
import com.academicmonitor.entity.WarningRecord;
import com.academicmonitor.entity.WarningRule;
import com.academicmonitor.repository.StudentRepository;
import com.academicmonitor.repository.WarningRecordRepository;
import com.academicmonitor.repository.WarningRuleRepository;
import com.academicmonitor.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
@RestController
@RequestMapping("/warnings")
public class WarningController {
    
    private static final Logger logger = LoggerFactory.getLogger(WarningController.class);
    
    @Autowired
    private WarningRecordRepository warningRecordRepository;
    
    @Autowired
    private WarningRuleRepository warningRuleRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    // 获取所有预警（分页）
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> getAllWarnings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String warningType,
            @RequestParam(required = false) String status) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("warningTime").descending());
            Page<WarningRecord> warnings;
            
            // 根据条件筛选
            if (studentName != null && !studentName.isEmpty()) {
                // 这里简化处理，实际应该使用更复杂的查询
                warnings = warningRecordRepository.findAll(pageable);
                // 然后在内存中过滤，这里需要分开处理
                List<WarningRecord> filteredList = warnings.stream()
                    .filter(w -> w.getStudent().getUser().getName().contains(studentName))
                    .collect(Collectors.toList());
                
                // 创建新的Page对象
                warnings = new org.springframework.data.domain.PageImpl<>(
                    filteredList, pageable, filteredList.size());
            } else if (warningType != null && !warningType.isEmpty()) {
                List<WarningRule> rules = warningRuleRepository.findByType(warningType);
                // 这里也简化处理
                Page<WarningRecord> allWarnings = warningRecordRepository.findAll(pageable);
                
                // 过滤并创建新的Page对象
                List<WarningRecord> filteredList = allWarnings.stream()
                    .filter(w -> rules.contains(w.getRule()))
                    .collect(Collectors.toList());
                
                warnings = new org.springframework.data.domain.PageImpl<>(
                    filteredList, pageable, filteredList.size());
            } else if (status != null && !status.isEmpty()) {
                // 检查findByStatus方法是否支持分页，如果不支持，需要先获取列表再手动分页
                List<WarningRecord> statusWarnings = warningRecordRepository.findByStatus(status);
                
                // 手动进行分页
                int start = (int)pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), statusWarnings.size());
                List<WarningRecord> paginatedList = statusWarnings.subList(start, end);
                
                warnings = new org.springframework.data.domain.PageImpl<>(
                    paginatedList, pageable, statusWarnings.size());
            } else {
                warnings = warningRecordRepository.findAll(pageable);
            }
            
            List<WarningResponse> content = warnings.getContent().stream()
                    .map(this::mapToWarningResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok().body(new org.springframework.data.domain.PageImpl<>(
                    content, pageable, warnings.getTotalElements()));
            
        } catch (Exception e) {
            logger.error("获取预警列表出错", e);
            return ResponseEntity.badRequest().body(new MessageResponse("获取预警列表失败：" + e.getMessage()));
        }
    }
    
    // 获取单个预警
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> getWarningById(@PathVariable Long id) {
        try {
            WarningRecord warning = warningRecordRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("未找到ID为 " + id + " 的预警记录"));
                    
            return ResponseEntity.ok().body(mapToWarningResponse(warning));
        } catch (Exception e) {
            logger.error("获取预警详情出错", e);
            return ResponseEntity.badRequest().body(new MessageResponse("获取预警详情失败：" + e.getMessage()));
        }
    }
    
    // 创建预警
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> createWarning(@Valid @RequestBody WarningRequest warningRequest) {
        try {
            WarningRecord warning = new WarningRecord();
            
            Student student = studentRepository.findById(warningRequest.getStudentId())
                    .orElseThrow(() -> new RuntimeException("未找到ID为 " + warningRequest.getStudentId() + " 的学生"));
                    
            WarningRule rule = warningRuleRepository.findById(warningRequest.getRuleId())
                    .orElseThrow(() -> new RuntimeException("未找到ID为 " + warningRequest.getRuleId() + " 的预警规则"));
            
            warning.setStudent(student);
            warning.setRule(rule);
            warning.setWarningContent(warningRequest.getContent());
            warning.setWarningTime(LocalDateTime.now());
            warning.setStatus("NEW");
            
            // 标记学生有预警
            student.setHasWarning(true);
            studentRepository.save(student);
            
            WarningRecord savedWarning = warningRecordRepository.save(warning);
            return ResponseEntity.ok().body(mapToWarningResponse(savedWarning));
        } catch (Exception e) {
            logger.error("创建预警出错", e);
            return ResponseEntity.badRequest().body(new MessageResponse("创建预警失败：" + e.getMessage()));
        }
    }
    
    // 更新预警
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> updateWarning(@PathVariable Long id, @Valid @RequestBody WarningRequest warningRequest) {
        try {
            WarningRecord warning = warningRecordRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("未找到ID为 " + id + " 的预警记录"));
                    
            // 更新可修改的字段
            warning.setWarningContent(warningRequest.getContent());
            
            if (warningRequest.getRuleId() != null) {
                WarningRule rule = warningRuleRepository.findById(warningRequest.getRuleId())
                        .orElseThrow(() -> new RuntimeException("未找到ID为 " + warningRequest.getRuleId() + " 的预警规则"));
                warning.setRule(rule);
            }
            
            WarningRecord updatedWarning = warningRecordRepository.save(warning);
            return ResponseEntity.ok().body(mapToWarningResponse(updatedWarning));
        } catch (Exception e) {
            logger.error("更新预警出错", e);
            return ResponseEntity.badRequest().body(new MessageResponse("更新预警失败：" + e.getMessage()));
        }
    }
    
    // 解决预警
    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> resolveWarning(@PathVariable Long id, @Valid @RequestBody ResolveWarningRequest resolveRequest) {
        try {
            WarningRecord warning = warningRecordRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("未找到ID为 " + id + " 的预警记录"));
                    
            warning.setStatus("RESOLVED");
            warning.setProcessedTime(LocalDateTime.now());
            warning.setProcessNotes(resolveRequest.getSolution());
            
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            // 这里应该通过userRepository获取User实体
            // 简化处理，不设置处理人
            // warning.setProcessedBy(user);
            
            WarningRecord resolvedWarning = warningRecordRepository.save(warning);
            
            // 检查学生是否还有其他未解决的预警
            List<WarningRecord> activeWarnings = warningRecordRepository.findByStudentAndStatus(
                    warning.getStudent(), "NEW");
            if (activeWarnings.isEmpty()) {
                Student student = warning.getStudent();
                student.setHasWarning(false);
                studentRepository.save(student);
            }
            
            return ResponseEntity.ok().body(mapToWarningResponse(resolvedWarning));
        } catch (Exception e) {
            logger.error("解决预警出错", e);
            return ResponseEntity.badRequest().body(new MessageResponse("解决预警失败：" + e.getMessage()));
        }
    }
    
    // 删除预警
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteWarning(@PathVariable Long id) {
        try {
            WarningRecord warning = warningRecordRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("未找到ID为 " + id + " 的预警记录"));
            
            Student student = warning.getStudent();
            warningRecordRepository.delete(warning);
            
            // 检查学生是否还有其他预警
            if (warningRecordRepository.findByStudentAndStatus(student, "NEW").isEmpty()) {
                student.setHasWarning(false);
                studentRepository.save(student);
            }
            
            return ResponseEntity.ok().body(new MessageResponse("预警删除成功"));
        } catch (Exception e) {
            logger.error("删除预警出错", e);
            return ResponseEntity.badRequest().body(new MessageResponse("删除预警失败：" + e.getMessage()));
        }
    }
    
    // 获取预警类型
    @GetMapping("/types")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> getWarningTypes() {
        try {
            List<WarningTypeResponse> types = new ArrayList<>();
            
            // 从预警规则中提取不同的类型
            List<WarningRule> rules = warningRuleRepository.findAll();
            rules.stream()
                .map(WarningRule::getType)
                .distinct()
                .forEach(type -> {
                    String name;
                    switch (type) {
                        case "COURSE_GRADE":
                            name = "单科成绩预警";
                            break;
                        case "MULTIPLE_FAIL":
                            name = "多科不及格预警";
                            break;
                        case "SEMESTER_AVERAGE":
                            name = "学期平均分预警";
                            break;
                        case "SEVERE":
                            name = "严重学业危机预警";
                            break;
                        default:
                            name = type;
                    }
                    types.add(new WarningTypeResponse(type, name));
                });
                
            return ResponseEntity.ok().body(types);
        } catch (Exception e) {
            logger.error("获取预警类型出错", e);
            return ResponseEntity.badRequest().body(new MessageResponse("获取预警类型失败：" + e.getMessage()));
        }
    }
    
    // 获取最近预警
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> getRecentWarnings(@RequestParam(defaultValue = "5") int limit) {
        try {
            PageRequest pageRequest = PageRequest.of(0, limit, Sort.by("warningTime").descending());
            Page<WarningRecord> recentWarnings = warningRecordRepository.findAll(pageRequest);
            
            List<WarningResponse> warnings = recentWarnings.getContent().stream()
                    .map(this::mapToWarningResponse)
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok().body(warnings);
        } catch (Exception e) {
            logger.error("获取最近预警出错", e);
            return ResponseEntity.badRequest().body(new MessageResponse("获取最近预警失败：" + e.getMessage()));
        }
    }
    
    // 获取学生的预警
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @userSecurity.isCurrentUserOrTeacher(#studentId)")
    public ResponseEntity<?> getStudentWarnings(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("未找到ID为 " + studentId + " 的学生"));
                    
            Pageable pageable = PageRequest.of(page, size, Sort.by("warningTime").descending());
            List<WarningRecord> studentWarnings = warningRecordRepository.findByStudent(student);
            
            // 手动进行分页
            int start = (int)pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), studentWarnings.size());
            
            List<WarningResponse> warnings = studentWarnings.subList(start, end).stream()
                    .map(this::mapToWarningResponse)
                    .collect(Collectors.toList());
                    
            Page<WarningResponse> warningsPage = new org.springframework.data.domain.PageImpl<>(
                    warnings, pageable, studentWarnings.size());
                    
            return ResponseEntity.ok().body(warningsPage);
        } catch (Exception e) {
            logger.error("获取学生预警出错", e);
            return ResponseEntity.badRequest().body(new MessageResponse("获取学生预警失败：" + e.getMessage()));
        }
    }
    
    // 将WarningRecord实体转换为WarningResponse
    private WarningResponse mapToWarningResponse(WarningRecord warning) {
        WarningResponse response = new WarningResponse();
        response.setId(warning.getId());
        response.setStudentId(warning.getStudent().getId());
        response.setStudentName(warning.getStudent().getUser().getName());
        response.setStudentNumber(warning.getStudent().getStudentNumber());
        
        response.setTitle(warning.getRule().getName());
        response.setWarningType(warning.getRule().getType());
        response.setLevel(warning.getRule().getLevel());
        response.setContent(warning.getWarningContent());
        response.setCreateTime(warning.getWarningTime());
        response.setStatus(warning.getStatus());
        
        if (warning.getProcessedTime() != null) {
            response.setProcessedTime(warning.getProcessedTime());
        }
        
        if (warning.getProcessNotes() != null) {
            response.setSolution(warning.getProcessNotes());
        }
        
        if (warning.getProcessedBy() != null) {
            response.setProcessedById(warning.getProcessedBy().getId());
            response.setProcessedByName(warning.getProcessedBy().getName());
        }
        
        return response;
    }
} 