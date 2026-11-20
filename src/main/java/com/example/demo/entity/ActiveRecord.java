package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface ActiveRecord extends CreatedRecord {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Boolean getIsActive();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Boolean getIsDeleted();

    static boolean isActiveRecord(ActiveRecord record) {
        return Boolean.TRUE.equals(record.getIsActive())
                && Boolean.FALSE.equals(record.getIsDeleted());
    }
}
