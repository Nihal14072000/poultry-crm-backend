package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.TenantId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle", schema = "demo")
@Data
@NoArgsConstructor
public class Vehicle implements ActiveRecord, AuditableRecord {
    @TenantId
    @JsonIgnore
    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name", length = 100)
    private String name;
    @Column(name = "veh_number", length = 50)
    private String vehNumber;
    @Column(name = "registration_no", length = 100)
    private String registrationNo;
    @Column(name = "reg_validity")
    private LocalDate regValidity;
    @Column(name = "insurance_no", length = 100)
    private String insuranceNo;
    @Column(name = "insurance_validity")
    private LocalDate insuranceValidity;
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
