package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Write contract for payment create/update operations.
 * System-managed identity, activity and audit fields are intentionally excluded.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentWriteRequest {
    private Integer buyingPartyId;
    private Integer actualOrderId;
    private Integer tripId;
    private String note;
    private String paymentType;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String externalReference;
    private Integer sellingPartyId;
    private String status;
}
