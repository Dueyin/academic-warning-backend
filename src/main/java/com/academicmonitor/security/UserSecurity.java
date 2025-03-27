package com.academicmonitor.security;

import com.academicmonitor.entity.Student;
import com.academicmonitor.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component("userSecurity")
public class UserSecurity {

    @Autowired
    private StudentRepository studentRepository;

    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userId.equals(userDetails.getId());
    }
    
    public boolean isCurrentUserOrTeacher(Long studentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // 如果是教师角色，直接返回true
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER") || 
                                                auth.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }
        
        // 否则检查是否是学生本人
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        // 找到学生对应的用户ID
        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isPresent()) {
            Long studentUserId = student.get().getUser().getId();
            return studentUserId.equals(currentUserId);
        }
        
        return false;
    }
} 