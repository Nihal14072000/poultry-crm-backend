package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PartyActualOrderDto(
        Integer id,
        Integer tripId,
        Integer orderTypeId,
        Integer qty,
        BigDecimal weight,
        BigDecimal rate,
        BigDecimal total,
        LocalDateTime createDate
) {
}
