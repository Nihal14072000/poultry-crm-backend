package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public interface TimestampedRecord extends CreatedRecord {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    LocalDateTime getCreateDate();

    void setCreateDate(LocalDateTime createDate);

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    LocalDateTime getUpdatedDate();

    void setUpdatedDate(LocalDateTime updatedDate);
}
