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
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 20)
    private String type; // SYSTEM, WARNING, ANNOUNCEMENT, MESSAGE

    @Column(length = 50)
    private String relatedObject; // 相关联的对象类型，如 COURSE, WARNING, GRADE 等

    @Column
    private Long relatedObjectId; // 相关联的对象ID

    @Column
    private Boolean isRead = false;

    @Column
    private LocalDateTime readTime;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column
    private LocalDateTime sendTime;
} 