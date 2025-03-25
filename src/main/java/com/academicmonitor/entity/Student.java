package com.academicmonitor.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "students")
public class Student extends BaseEntity {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(length = 20, unique = true)
    private String studentNumber;

    @Column
    private LocalDate dateOfBirth;

    @Column(length = 10)
    private String gender;

    @Column(length = 100)
    private String address;

    @ManyToOne
    @JoinColumn(name = "class_id")
    private ClassGroup classGroup;

    @Column
    private Boolean hasWarning = false;

    @Column(length = 255)
    private String emergencyContact;

    @Column(length = 20)
    private String emergencyPhone;
} 