package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "trip_party_relation", schema = "demo")
@Data
@NoArgsConstructor
public class TripPartyRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relation_id")
    private Integer relationId;

    @Column(name = "trip_id", nullable = false)
    private Integer tripId;

    @Column(name = "party_id", nullable = false)
    private Integer partyId;

    @Column(name = "party_type")
    private String partyType;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "createdate")
    private LocalDateTime createDate;

    @Column(name = "updateddate")
    private LocalDateTime updatedDate;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;
}