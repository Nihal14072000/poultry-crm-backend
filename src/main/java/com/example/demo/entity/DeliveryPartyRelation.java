package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "delievery_party_relation", schema = "demo")
@Data
@NoArgsConstructor
public class DeliveryPartyRelation {
    @Id
    private Integer id;

    @Column(name = "delivery_id")
    private Integer deliveryId;

    @Column(name = "party_id")
    private Integer partyId;

    @Column(name = "party_type", length = 50)
    private String partyType;

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
