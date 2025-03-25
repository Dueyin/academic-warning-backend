package com.academicmonitor.repository;

import com.academicmonitor.entity.Student;
import com.academicmonitor.entity.WarningRecord;
import com.academicmonitor.entity.WarningRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarningRecordRepository extends JpaRepository<WarningRecord, Long> {
    List<WarningRecord> findByStudent(Student student);
    
    List<WarningRecord> findByRule(WarningRule rule);
    
    List<WarningRecord> findByStatus(String status);
    
    List<WarningRecord> findByStudentAndStatus(Student student, String status);
} 