package com.example.demo.dto;

import com.example.demo.entity.Expenses;
import com.example.demo.entity.Mortality;
import com.example.demo.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripDeliveryDetailsResponse {
    private TripRetrieveResponse trip;
    private List<SupplierPickupRetrieveResponse> supplierPickups;
    private List<DeliveryRetrieveResponse> deliveries;
    private List<Mortality> mortalities;
    private List<Expenses> expenses;
    private List<Payment> payments;
}
