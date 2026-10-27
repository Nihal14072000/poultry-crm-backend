package com.example.demo.dto;

import com.example.demo.entity.Expenses;
import com.example.demo.entity.Mortality;
import com.example.demo.entity.Payment;
import com.example.demo.entity.SupplierPickup;
import com.example.demo.entity.Trip;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRetrieveResponse {
    private Integer id;
    private Trip trip;
    private SupplierPickup supplierPickup;
    private Mortality mortality;
    private Payment payment;
    private Expenses expenses;
    private Integer status;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createDate;
    private LocalDateTime updatedDate;
    private Integer createdBy;
    private Integer updatedBy;
    private List<VendorDeliveryRetrieveResponse> vendorDeliveries;
}
