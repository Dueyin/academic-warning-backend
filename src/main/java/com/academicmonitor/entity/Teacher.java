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
@Table(name = "teachers")
public class Teacher extends BaseEntity {

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(length = 20, unique = true)
    private String teacherNumber;

    @Column(length = 100)
    private String title;

    @Column(length = 255)
    private String specialty;

    @OneToMany(mappedBy = "headTeacher")
    private Set<ClassGroup> headOfClasses = new HashSet<>();

    @OneToMany(mappedBy = "teacher")
    private Set<Course> courses = new HashSet<>();
} 