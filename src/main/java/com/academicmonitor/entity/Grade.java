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
@Table(name = "grades")
public class Grade extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "enrollment_id")
    private CourseEnrollment enrollment;

    @Column(length = 50, nullable = false)
    private String gradeType; // 例如：期中考试、期末考试、作业、项目等

    @Column
    private Double score;

    @Column
    private Double maxScore = 100.0;

    @Column
    private Integer weight; // 权重，百分比形式，如期末考试权重 60 表示 60%

    @Column
    private LocalDate examDate;

    @Column(length = 255)
    private String comments;

    @ManyToOne
    @JoinColumn(name = "recorded_by")
    private User recordedBy;
} 