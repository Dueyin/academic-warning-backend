package com.academicmonitor.repository;

import com.academicmonitor.entity.Course;
import com.academicmonitor.entity.CourseEnrollment;
import com.academicmonitor.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    List<CourseEnrollment> findByStudent(Student student);
    
    List<CourseEnrollment> findByCourse(Course course);
    
    List<CourseEnrollment> findByStudentAndSemester(Student student, String semester);
    
    List<CourseEnrollment> findByCourseAndSemester(Course course, String semester);
    
    Optional<CourseEnrollment> findByStudentAndCourse(Student student, Course course);
} 