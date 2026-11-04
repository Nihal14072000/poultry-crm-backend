package com.example.demo.dto;

import com.example.demo.entity.FinancePaymentAllocation;
import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRetrieveResponse {
    private Integer id;
    private Integer buyingPartyId;
    private BuyingPartydto buyingParty;
    private Integer actualOrderId;
    private Order actualOrder;
    private Integer tripId;
    private TripRetrieveResponse trip;
    private String note;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createDate;
    private LocalDateTime updatedDate;
    private Integer createdBy;
    private Integer updatedBy;
    private String paymentType;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String externalReference;
    private Integer sellingPartyId;
    private SellingPartydto sellingParty;
    private String status;
    private List<FinancePaymentAllocation> allocations;

    public static PaymentRetrieveResponse from(
            Payment payment,
            BuyingPartydto buyingParty,
            Order actualOrder,
            TripRetrieveResponse trip,
            SellingPartydto sellingParty,
            List<FinancePaymentAllocation> allocations
    ) {
        return PaymentRetrieveResponse.builder()
                .id(payment.getId())
                .buyingPartyId(payment.getBuyingPartyId())
                .buyingParty(buyingParty)
                .actualOrderId(payment.getActualOrderId())
                .actualOrder(actualOrder)
                .tripId(payment.getTripId())
                .trip(trip)
                .note(payment.getNote())
                .isActive(payment.getIsActive())
                .isDeleted(payment.getIsDeleted())
                .createDate(payment.getCreateDate())
                .updatedDate(payment.getUpdatedDate())
                .createdBy(payment.getCreatedBy())
                .updatedBy(payment.getUpdatedBy())
                .paymentType(payment.getPaymentType())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .externalReference(payment.getExternalReference())
                .sellingPartyId(payment.getSellingPartyId())
                .sellingParty(sellingParty)
                .status(payment.getStatus())
                .allocations(allocations)
                .build();
    }
}
