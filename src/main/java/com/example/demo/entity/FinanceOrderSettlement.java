package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "finance_order_settlement", schema = "demo")
@Data
@NoArgsConstructor
public class FinanceOrderSettlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "trip_id", nullable = false)
    private Integer tripId;
    @Column(name = "order_id", nullable = false)
    private Integer orderId;
    @Column(name = "buying_party_id", nullable = false)
    private Integer buyingPartyId;
    @Column(name = "selling_party_id", nullable = false)
    private Integer sellingPartyId;
    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;
    @Column(name = "due_date")
    private LocalDate dueDate;
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode = "INR";
    @Column(name = "order_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal orderAmount;
    @Column(name = "brokerage_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal brokerageAmount = BigDecimal.ZERO;
    @Column(name = "brokerage_payer", nullable = false, length = 10)
    private String brokeragePayer;
    @Column(insertable = false, updatable = false, precision = 14, scale = 2)
    private BigDecimal buyerDueAmount;
    @Column(insertable = false, updatable = false, precision = 14, scale = 2)
    private BigDecimal sellerDueAmount;
    @Column(nullable = false, length = 20)
    private String status = "OPEN";
    private String notes;
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}
