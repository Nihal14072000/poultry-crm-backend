package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"order\"", schema = "demo")
@Data
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "order_type_id")
    private Integer orderTypeId;
    private Integer qty;
    @Column(precision = 10, scale = 2)
    private BigDecimal weight;
    @Column(precision = 10, scale = 2)
    private BigDecimal rate;
    @Column(precision = 12, scale = 2)
    private BigDecimal total;
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
}
