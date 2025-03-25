package com.academicmonitor.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "warning_records")
public class WarningRecord extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "rule_id")
    private WarningRule rule;

    @Column(nullable = false)
    private String warningContent;

    @Column
    private LocalDateTime warningTime;

    @Column(length = 20)
    private String status; // NEW, READ, PROCESSED, RESOLVED

    @Column
    private LocalDateTime processedTime;

    @ManyToOne
    @JoinColumn(name = "processed_by")
    private User processedBy;

    @Column(length = 255)
    private String processNotes;
} 