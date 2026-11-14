package com.example.demo.entity;

public interface AuditableRecord extends TimestampedRecord {
    Integer getCreatedBy();

    void setCreatedBy(Integer createdBy);

    Integer getUpdatedBy();

    void setUpdatedBy(Integer updatedBy);
}
