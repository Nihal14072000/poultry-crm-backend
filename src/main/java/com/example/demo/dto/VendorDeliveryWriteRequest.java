package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Write contract for vendor delivery operations. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VendorDeliveryWriteRequest {
    private Integer sellingPartyId;
    private Integer actualOrderId;
    private String note;
}
