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
@Table(name = "class_groups")
public class ClassGroup extends BaseEntity {

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 4, nullable = false)
    private String year;

    @Column(length = 100)
    private String description;
    
    @Column(length = 100)
    private String college;
    
    @Column(length = 100)
    private String major;

    @ManyToOne
    @JoinColumn(name = "head_teacher_id")
    private Teacher headTeacher;

    @OneToMany(mappedBy = "classGroup")
    private Set<Student> students = new HashSet<>();
} 