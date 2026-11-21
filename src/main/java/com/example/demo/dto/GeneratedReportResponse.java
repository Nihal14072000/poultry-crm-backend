package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GeneratedReportResponse(
        Long id,
        String reportType,
        LocalDate fromDate,
        LocalDate toDate,
        String format,
        String status,
        String fileName,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {}
