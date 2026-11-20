package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Write contract for supplier pickup operations. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupplierPickupWriteRequest {
    private Integer buyingPartyId;
    private Integer actualOrderId;
    private String note;
}
