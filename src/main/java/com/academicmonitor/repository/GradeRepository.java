package com.academicmonitor.repository;

import com.academicmonitor.entity.CourseEnrollment;
import com.academicmonitor.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByEnrollment(CourseEnrollment enrollment);
    
    List<Grade> findByEnrollmentAndGradeType(CourseEnrollment enrollment, String gradeType);
    
    @Query("SELECT g FROM Grade g WHERE g.enrollment.student.id = :studentId AND g.enrollment.course.id = :courseId")
    List<Grade> findByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    @Query("SELECT g FROM Grade g WHERE g.enrollment.student.id = :studentId")
    List<Grade> findByStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT g FROM Grade g WHERE g.enrollment.course.id = :courseId")
    List<Grade> findByCourse(@Param("courseId") Long courseId);
    
    @Query("SELECT AVG(g.score) FROM Grade g WHERE g.enrollment.student.id = :studentId AND g.enrollment.semester = :semester")
    Double findAverageScoreByStudentAndSemester(@Param("studentId") Long studentId, @Param("semester") String semester);
} 