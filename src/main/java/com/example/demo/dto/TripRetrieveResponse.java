package com.example.demo.dto;

import com.example.demo.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripRetrieveResponse {
    private Integer tripId;
    private Vehicle vehicle;
    private Employee driver;
    private Employee supervisor;
    private List<BuyingPartydto> buyingParties;
    private List<SellingPartydto> sellingParties;
    private String route;
    private LocalDate date;
    private Integer pickupId;
    private Integer dropId;
    private String status;
    private LocalDateTime createDate;
    private LocalDateTime updatedDate;
    private Integer createdBy;
    private Integer updatedBy;

}
