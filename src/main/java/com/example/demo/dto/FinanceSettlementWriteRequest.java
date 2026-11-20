package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Allowlisted input for creating a finance settlement. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinanceSettlementWriteRequest {
    private Integer tripId;
    private Integer orderId;
    private Integer buyingPartyId;
    private Integer sellingPartyId;
    private LocalDate settlementDate;
    private LocalDate dueDate;
    private String currencyCode;
    private BigDecimal orderAmount;
    private BigDecimal brokerageAmount;
    private String brokeragePayer;
    private String notes;
}
