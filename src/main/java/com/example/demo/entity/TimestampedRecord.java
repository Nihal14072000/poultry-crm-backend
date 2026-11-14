package com.example.demo.entity;

import java.time.LocalDateTime;

public interface TimestampedRecord extends CreatedRecord {
    void setCreateDate(LocalDateTime createDate);

    void setUpdatedDate(LocalDateTime updatedDate);
}
