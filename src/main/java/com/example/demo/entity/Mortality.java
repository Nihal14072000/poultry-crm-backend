package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mortality", schema = "demo")
@Data
@NoArgsConstructor
public class Mortality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "trip_id")
    private Integer tripId;

    @Column(name = "combined_mortality")
    private Integer combinedMortality;

    @Column(length = 255)
    private String reason;

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
