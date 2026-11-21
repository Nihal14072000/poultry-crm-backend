package com.example.demo.report;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ReportData(String title, List<String> columns, List<Map<String, Object>> rows,
                         long recordCount, BigDecimal totalAmount, BigDecimal totalQuantity) {}
