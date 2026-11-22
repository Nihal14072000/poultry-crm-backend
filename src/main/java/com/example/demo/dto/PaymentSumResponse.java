package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentSumResponse(
        Integer partyId,
        String partyType,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalAmount
) {
}
