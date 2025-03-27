package com.academicmonitor.controller;

import com.academicmonitor.dto.response.DashboardStatistics;
import com.academicmonitor.dto.response.MessageResponse;
import com.academicmonitor.dto.response.WarningTrendResponse;
import com.academicmonitor.entity.Course;
import com.academicmonitor.entity.Student;
import com.academicmonitor.entity.WarningRecord;
import com.academicmonitor.repository.CourseRepository;
import com.academicmonitor.repository.StudentRepository;
import com.academicmonitor.repository.WarningRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    
    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private WarningRecordRepository warningRecordRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    // 获取仪表盘统计数据
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> getDashboardStatistics() {
        try {
            DashboardStatistics stats = new DashboardStatistics();
            
            // 计算学生总数
            long totalStudents = studentRepository.count();
            stats.setTotalStudents(totalStudents);
            
            // 计算预警总数
            long totalWarnings = warningRecordRepository.count();
            stats.setTotalWarnings(totalWarnings);
            
            // 计算已解决的预警数量
            long resolvedWarnings = warningRecordRepository.findByStatus("RESOLVED").size();
            stats.setResolvedWarnings(resolvedWarnings);
            
            // 计算课程总数
            long totalCourses = courseRepository.count();
            stats.setTotalCourses(totalCourses);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("获取仪表盘统计数据出错", e);
            return ResponseEntity.badRequest().body(new MessageResponse("获取仪表盘统计数据失败：" + e.getMessage()));
        }
    }
    
    // 获取预警类型分布
    @GetMapping("/warnings/distribution")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> getWarningTypeDistribution() {
        try {
            // 获取所有预警记录
            List<WarningRecord> warnings = warningRecordRepository.findAll();
            
            // 按预警类型分组并计数
            Map<String, Long> distribution = warnings.stream()
                    .collect(Collectors.groupingBy(
                            warning -> warning.getRule().getType(),
                            Collectors.counting()
                    ));
            
            // 确保所有类型都有条目，即使没有数据
            String[] allTypes = {"COURSE_GRADE", "MULTIPLE_FAIL", "SEMESTER_AVERAGE", "SEVERE"};
            for (String type : allTypes) {
                distribution.putIfAbsent(type, 0L);
            }
            
            return ResponseEntity.ok(distribution);
        } catch (Exception e) {
            logger.error("获取预警类型分布出错", e);
            return ResponseEntity.badRequest().body(new MessageResponse("获取预警类型分布失败：" + e.getMessage()));
        }
    }
    
    // 获取预警趋势
    @GetMapping("/warnings/trends")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> getWarningTrends(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(defaultValue = "6") int months) {
        try {
            // 获取所有预警记录
            List<WarningRecord> allWarnings = warningRecordRepository.findAll();
            
            // 设置日期范围
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusMonths(months);
            
            // 筛选日期范围内的预警
            List<WarningRecord> warnings = allWarnings.stream()
                    .filter(w -> w.getWarningTime() != null && 
                            w.getWarningTime().isAfter(startDate) && 
                            w.getWarningTime().isBefore(endDate))
                    .collect(Collectors.toList());
            
            List<WarningTrendResponse> trends = new ArrayList<>();
            DateTimeFormatter formatter;
            
            // 根据周期格式化日期
            if ("day".equals(period)) {
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                // 按天分组
                Map<String, List<WarningRecord>> groupedByDay = warnings.stream()
                        .collect(Collectors.groupingBy(
                                w -> w.getWarningTime().format(formatter)
                        ));
                
                // 计算每天的预警数量和已解决数量
                for (int i = 0; i < 30; i++) {
                    LocalDate date = endDate.toLocalDate().minusDays(i);
                    String dateStr = date.format(formatter);
                    List<WarningRecord> dayWarnings = groupedByDay.getOrDefault(dateStr, Collections.emptyList());
                    
                    long resolvedCount = dayWarnings.stream()
                            .filter(w -> "RESOLVED".equals(w.getStatus()))
                            .count();
                            
                    trends.add(new WarningTrendResponse(dateStr, dayWarnings.size(), resolvedCount));
                }
            } else if ("week".equals(period)) {
                // 按周分组（简化处理）
                for (int i = 0; i < 12; i++) {
                    LocalDateTime weekStart = endDate.minusWeeks(i);
                    LocalDateTime weekEnd = weekStart.plusWeeks(1);
                    String weekLabel = "W" + (endDate.toLocalDate().getMonth().getValue()) + "-" + (i + 1);
                    
                    List<WarningRecord> weekWarnings = warnings.stream()
                            .filter(w -> w.getWarningTime().isAfter(weekStart) && 
                                    w.getWarningTime().isBefore(weekEnd))
                            .collect(Collectors.toList());
                            
                    long resolvedCount = weekWarnings.stream()
                            .filter(w -> "RESOLVED".equals(w.getStatus()))
                            .count();
                            
                    trends.add(new WarningTrendResponse(weekLabel, weekWarnings.size(), resolvedCount));
                }
            } else {
                // 默认按月分组
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                Map<String, List<WarningRecord>> groupedByMonth = warnings.stream()
                        .collect(Collectors.groupingBy(
                                w -> w.getWarningTime().format(formatter)
                        ));
                
                // 计算每月的预警数量和已解决数量
                for (int i = 0; i < months; i++) {
                    LocalDateTime monthStart = endDate.minusMonths(i);
                    String monthStr = monthStart.format(formatter);
                    List<WarningRecord> monthWarnings = groupedByMonth.getOrDefault(monthStr, Collections.emptyList());
                    
                    long resolvedCount = monthWarnings.stream()
                            .filter(w -> "RESOLVED".equals(w.getStatus()))
                            .count();
                            
                    trends.add(new WarningTrendResponse(monthStr, monthWarnings.size(), resolvedCount));
                }
            }
            
            // 倒序返回，使日期从早到晚
            Collections.reverse(trends);
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            logger.error("获取预警趋势出错", e);
            return ResponseEntity.badRequest().body(new MessageResponse("获取预警趋势失败：" + e.getMessage()));
        }
    }
} 