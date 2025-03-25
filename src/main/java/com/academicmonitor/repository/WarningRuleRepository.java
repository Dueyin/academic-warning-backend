package com.academicmonitor.repository;

import com.academicmonitor.entity.WarningRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarningRuleRepository extends JpaRepository<WarningRule, Long> {
    List<WarningRule> findByIsActiveTrue();
    
    List<WarningRule> findByType(String type);
    
    List<WarningRule> findByLevel(Integer level);
} 