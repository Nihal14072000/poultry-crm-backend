package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface AuditableRecord extends TimestampedRecord {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Integer getCreatedBy();

    void setCreatedBy(Integer createdBy);

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Integer getUpdatedBy();

    void setUpdatedBy(Integer updatedBy);
}
