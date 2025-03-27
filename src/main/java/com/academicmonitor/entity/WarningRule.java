package com.academicmonitor.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "warning_rules")
public class WarningRule extends BaseEntity {

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(length = 50, nullable = false)
    private String type; // GPA、单科成绩、连续不及格次数、出勤率等

    @Column(name = "rule_condition", nullable = false)
    private String condition; // 例如 "score < 60"、"absences > 3" 等

    @Column(nullable = false)
    private Integer level; // 预警级别：1=一般预警，2=中度预警，3=严重预警

    @Column(length = 50)
    private String color; // 预警颜色：黄色、橙色、红色等

    @Column
    private Boolean isActive = true;
} 