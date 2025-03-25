package com.academicmonitor.repository;

import com.academicmonitor.entity.ClassGroup;
import com.academicmonitor.entity.Student;
import com.academicmonitor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUser(User user);
    
    Optional<Student> findByStudentNumber(String studentNumber);
    
    List<Student> findByClassGroup(ClassGroup classGroup);
    
    @Query("SELECT s FROM Student s WHERE s.hasWarning = true")
    List<Student> findAllWithWarnings();
    
    boolean existsByStudentNumber(String studentNumber);
} 