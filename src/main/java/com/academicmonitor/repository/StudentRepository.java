package com.academicmonitor.repository;

import com.academicmonitor.entity.ClassGroup;
import com.academicmonitor.entity.Student;
import com.academicmonitor.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    Optional<Student> findByUser(User user);
    
    Optional<Student> findByStudentNumber(String studentNumber);
    
    List<Student> findByClassGroup(ClassGroup classGroup);
    
    @Query("SELECT s FROM Student s WHERE s.hasWarning = true")
    List<Student> findAllWithWarnings();
    
    boolean existsByStudentNumber(String studentNumber);
    
    /**
     * 根据多个条件查询学生并分页
     * @param studentNumber 学号（可选）
     * @param name 姓名（可选）
     * @param college 学院（可选）
     * @param major 专业（可选）
     * @param pageable 分页参数
     * @return 分页后的学生列表
     */
    @Query("SELECT s FROM Student s JOIN s.user u " +
           "LEFT JOIN s.classGroup c " +
           "WHERE (:studentNumber IS NULL OR :studentNumber = '' OR s.studentNumber LIKE %:studentNumber%) " +
           "AND (:name IS NULL OR :name = '' OR u.name LIKE %:name%) " +
           "AND (:college IS NULL OR :college = '' OR c.college LIKE %:college% OR c IS NULL) " +
           "AND (:major IS NULL OR :major = '' OR c.major LIKE %:major% OR c IS NULL)")
    Page<Student> findByFilters(@Param("studentNumber") String studentNumber, 
                               @Param("name") String name,
                               @Param("college") String college,
                               @Param("major") String major,
                               Pageable pageable);
} 