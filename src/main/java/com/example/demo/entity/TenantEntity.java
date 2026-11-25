package com.example.demo.entity;

import com.example.demo.security.TenantContext;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class TenantEntity {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @PrePersist
    void assignTenantId() {
        if (tenantId == null) {
            tenantId = TenantContext.currentTenantIdOrDefault();
        }
    }
}
