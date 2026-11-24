package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.TenantId;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "delievery", schema = "demo")
@Data
@NoArgsConstructor
public class Delivery implements ActiveRecord, AuditableRecord {
    @TenantId
    @JsonIgnore
    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "trip_id")
    private Integer tripId;

    @Column(name = "supplier_pickup_id")
    private Integer supplierPickupId;

    @Column(name = "vendor_delievery_id")
    private Integer vendorDelieveryId;

    @Column(name = "mortality_id")
    private Integer mortalityId;

    @Column(name = "payment_id")
    private Integer paymentId;

    @Column(name = "expense_id")
    private Integer expenseId;

    private Integer status;

    @Column(name = "isactive")
    private Boolean isActive = true;

    @Column(name = "isdeleted")
    private Boolean isDeleted = false;

    @Column(name = "createdate")
    private LocalDateTime createDate;

    @Column(name = "updateddate")
    private LocalDateTime updatedDate;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Override
    public LocalDateTime getCreatedAtForSort() {
        return createDate;
    }
}
