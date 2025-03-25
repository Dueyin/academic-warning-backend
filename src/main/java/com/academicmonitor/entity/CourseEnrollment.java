package com.academicmonitor.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course_enrollments")
public class CourseEnrollment extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(length = 50)
    private String semester;

    @Column
    private Boolean isActive = true;

    @OneToMany(mappedBy = "enrollment")
    private Set<Grade> grades = new HashSet<>();
} 