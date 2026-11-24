package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenant", schema = "demo")
@Getter
@Setter
@NoArgsConstructor
public class Tenant {
    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "owner_name", nullable = false, length = 250)
    private String ownerName;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
