package com.example.demo.dto;

import com.example.demo.entity.BuyingParty;
import com.example.demo.entity.Order;
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
public class SupplierPickupRetrieveResponse {
    private Integer id;
    private BuyingParty buyingParty;
    private List<Order> actualOrders;
    private String note;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createDate;
    private LocalDateTime updatedDate;
    private Integer createdBy;
    private Integer updatedBy;
}
