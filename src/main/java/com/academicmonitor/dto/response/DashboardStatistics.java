package com.academicmonitor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatistics {
    private long totalStudents;
    private long totalWarnings;
    private long resolvedWarnings;
    private long totalCourses;
} 