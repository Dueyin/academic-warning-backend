package com.academicmonitor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarningTrendResponse {
    private String date;
    private long count;
    private long resolvedCount;
} 