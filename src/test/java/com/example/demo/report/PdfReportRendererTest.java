package com.example.demo.report;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfReportRendererTest {
    @Test
    void rendersAValidPdfWithReportHeadingAndRows() {
        ReportJob job = new ReportJob(1L, 1L, "SALES", LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 21), "PDF", "PROCESSING", Map.of(), 7,
                null, null, null, LocalDate.now().atStartOfDay(), null);
        ReportData data = new ReportData("Sales Report", List.of("report_date", "party_name", "amount"),
                List.of(Map.of("report_date", java.sql.Date.valueOf("2026-11-02"),
                        "party_name", "ABC Traders", "amount", new BigDecimal("45000.00"))),
                1, new BigDecimal("45000.00"), new BigDecimal("125.00"));

        byte[] pdf = new PdfReportRenderer().render(job, data);

        assertTrue(pdf.length > 500);
        assertTrue(new String(pdf, 0, 8, java.nio.charset.StandardCharsets.US_ASCII).startsWith("%PDF-"));
    }
}
