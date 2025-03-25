package com.academicmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AcademicMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcademicMonitoringApplication.class, args);
    }
} 