package com.example.demo.report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public record ReportJob(
        Long id,
        String reportType,
        LocalDate fromDate,
        LocalDate toDate,
        String format,
        String status,
        Map<String, Object> filters,
        Integer requestedBy,
        String tenantId,
        String filePath,
        String fileName,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {}
