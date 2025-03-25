package com.academicmonitor.repository;

import com.academicmonitor.entity.Teacher;
import com.academicmonitor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUser(User user);
    
    Optional<Teacher> findByTeacherNumber(String teacherNumber);
    
    boolean existsByTeacherNumber(String teacherNumber);
} 