package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Allowlisted input for allocating a completed payment. Allocation type is derived by the server. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinanceAllocationWriteRequest {
    private Integer paymentId;
    private Integer settlementId;
    private BigDecimal allocatedAmount;
    private LocalDate allocationDate;
    private String note;
}
