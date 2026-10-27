package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee", schema = "demo")
@Data
@NoArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name", length = 250)
    private String name;
    @Column(length = 250)
    private String address;
    @Column(name = "number", length = 250)
    private String number;
    @Column(name = "type", length = 250)
    private String type;
    @Column(length = 250)
    private String status;
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
