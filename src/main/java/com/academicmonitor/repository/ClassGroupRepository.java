package com.academicmonitor.repository;

import com.academicmonitor.entity.ClassGroup;
import com.academicmonitor.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassGroupRepository extends JpaRepository<ClassGroup, Long> {
    List<ClassGroup> findByHeadTeacher(Teacher headTeacher);
    
    List<ClassGroup> findByYear(String year);
} 