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
@Table(name = "payment", schema = "demo")
@Data
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_id_seq")
    @jakarta.persistence.SequenceGenerator(name = "payment_id_seq", sequenceName = "payment_id_seq", schema = "demo", allocationSize = 1)
    private Integer id;

    @Column(name = "buying_party_id")
    private Integer buyingPartyId;

    @Column(name = "actual_order_id")
    private Integer actualOrderId;

    @Column(name = "trip_id")
    private Integer tripId;

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

    @Column(name = "payment_type", length = 20)
    private String paymentType;

    @Column(precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "external_reference", length = 100)
    private String externalReference;

    @Column(name = "selling_party_id")
    private Integer sellingPartyId;

    @Column(length = 20)
    private String status = "PENDING";
}
