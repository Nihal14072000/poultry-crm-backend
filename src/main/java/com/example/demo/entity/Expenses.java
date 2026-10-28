package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "expenses", schema = "demo")
@Data
@NoArgsConstructor
public class Expenses {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "expenses_id_seq")
    @jakarta.persistence.SequenceGenerator(name = "expenses_id_seq", sequenceName = "expenses_id_seq", schema = "demo", allocationSize = 1)
    private Integer id;

    @Column(name = "trip_id")
    private Integer tripId;

    @Column(precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date")
    private LocalDate expenseDate;

    @Column(length = 60)
    private String category;

    @Column(name = "selling_party_id")
    private Integer sellingPartyId;

    @Column(name = "actual_order_id")
    private Integer actualOrderId;

    @Column(length = 500)
    private String note;

    @Column(name = "isactive")
    private Boolean isActive = true;

    @Column(name = "isdeleted")
    private Boolean isDeleted = false;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;
}
