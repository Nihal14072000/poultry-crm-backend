package com.example.demo.entity;

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
@Table(name = "trip", schema = "demo")
@Data
@NoArgsConstructor
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Integer tripId;
    @Column(name = "vehicle_id")
    private Integer vehicleId;
    @Column(name = "driver_id")
    private Integer driverId;
    @Column(name = "supervisor_id")
    private Integer supervisorId;
    private String route;
    @Column(name = "\"date\"")
    private LocalDate date;
    @Column(name = "pickup_id")
    private Integer pickupId;
    @Column(name = "drop_id")
    private Integer dropId;
    @Column(length = 50)
    private String status;
    @Column(name = "createdate")
    private LocalDateTime createDate;
    @Column(name = "updateddate")
    private LocalDateTime updatedDate;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "updated_by")
    private Integer updatedBy;
    @Column(name = "buying_party_id")
    private Integer buyingPartyId;
    @Column(name = "selling_party_id")
    private Integer sellingPartyId;
}
