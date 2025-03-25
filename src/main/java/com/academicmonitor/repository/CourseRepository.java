package com.academicmonitor.repository;

import com.academicmonitor.entity.Course;
import com.academicmonitor.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
    
    List<Course> findByTeacher(Teacher teacher);
    
    List<Course> findBySemester(String semester);
    
    boolean existsByCourseCode(String courseCode);
} 