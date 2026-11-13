package com.example.demo.dto;

import java.math.BigDecimal;

public record FinanceSummaryResponse(
        BigDecimal totalReceived,
        BigDecimal totalReceivable,
        BigDecimal totalPaid,
        BigDecimal totalPayable
) {
}
