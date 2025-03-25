package com.academicmonitor.repository;

import com.academicmonitor.entity.Notification;
import com.academicmonitor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);
    
    List<Notification> findByUserAndIsReadFalse(User user);
    
    List<Notification> findByUserAndType(User user, String type);
    
    List<Notification> findBySender(User sender);
    
    List<Notification> findByRelatedObjectAndRelatedObjectId(String relatedObject, Long relatedObjectId);
} 