package com.example.demo.entity;

/** Marks persisted business records whose rows belong to exactly one tenant. */
public interface TenantScopedRecord {
    String getTenantId();
    void setTenantId(String tenantId);
}
