package com.example.demo.dto;

import com.example.demo.report.ReportFormat;
import com.example.demo.report.ReportType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Map;

public record ReportGenerationRequest(
        @NotNull ReportType reportType,
        LocalDate fromDate,
        LocalDate toDate,
        @NotNull ReportFormat format,
        Map<String, Object> filters
) {}
