package com.example.demo.dto;

import com.example.demo.entity.Order;
import com.example.demo.entity.SellingParty;
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
public class VendorDeliveryRetrieveResponse {
    private Integer relationId;
    private Integer id;
    private SellingParty sellingParty;
    private List<Order> actualOrders;
    private String note;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createDate;
    private LocalDateTime updatedDate;
    private Integer createdBy;
    private Integer updatedBy;
}
