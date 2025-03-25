package com.academicmonitor.controller;

import com.academicmonitor.entity.User;
import com.academicmonitor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/hello")
    public String hello() {
        return "Hello, API is working!";
    }
    
    @GetMapping("/checkUser")
    public Map<String, Object> checkUser(@RequestParam String username) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            result.put("exists", true);
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("enabled", user.getEnabled());
            result.put("roles", user.getRoles());
            result.put("passwordLength", user.getPassword().length());
        } else {
            result.put("exists", false);
        }
        
        return result;
    }
    
    @GetMapping("/checkPassword")
    public Map<String, Object> checkPassword(@RequestParam String username, @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean matches = passwordEncoder.matches(password, user.getPassword());
            result.put("exists", true);
            result.put("passwordMatches", matches);
            result.put("storedHashLength", user.getPassword().length());
            result.put("storedHash", user.getPassword());
        } else {
            result.put("exists", false);
        }
        
        return result;
    }
} 