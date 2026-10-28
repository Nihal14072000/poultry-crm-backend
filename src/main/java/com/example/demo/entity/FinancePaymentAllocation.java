package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "finance_payment_allocation", schema = "demo")
@Data
@NoArgsConstructor
public class FinancePaymentAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "payment_id", nullable = false)
    private Integer paymentId;
    @Column(name = "settlement_id", nullable = false)
    private Integer settlementId;
    @Column(name = "allocation_type", nullable = false, length = 20)
    private String allocationType;
    @Column(name = "allocated_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal allocatedAmount;
    @Column(name = "allocation_date", nullable = false)
    private LocalDate allocationDate;
    @Column(length = 500)
    private String note;
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
