package com.academicmonitor.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/open")
public class SimpleController {
    private static final Logger logger = LoggerFactory.getLogger(SimpleController.class);

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        logger.info("访问了/open/hello端点");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello, 这是一个完全开放的端点!");
        response.put("status", "success");
        return response;
    }
    
    @PostMapping("/test")
    public Map<String, Object> test() {
        logger.info("访问了/open/test端点");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "这是一个POST请求测试");
        response.put("status", "success");
        return response;
    }
} 