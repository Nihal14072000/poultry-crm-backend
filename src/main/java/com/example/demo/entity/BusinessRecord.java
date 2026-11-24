package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "business_record", schema = "demo")
public class BusinessRecord implements TenantScopedRecord {
    @Id
    private UUID id;

    @TenantId
    @JsonIgnore
    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 64)
    private String resource;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "record_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> data;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected BusinessRecord() {}

    public BusinessRecord(UUID id, String resource, Map<String, Object> data) {
        this.id = id;
        this.resource = resource;
        this.data = data;
    }

    @PrePersist
    void beforeInsert() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override public String getTenantId() { return tenantId; }
    @Override public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public UUID getId() { return id; }
    public String getResource() { return resource; }
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
}
