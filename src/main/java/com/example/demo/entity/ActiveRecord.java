package com.example.demo.entity;

public interface ActiveRecord extends CreatedRecord {
    Boolean getIsActive();

    Boolean getIsDeleted();

    static boolean isActiveRecord(ActiveRecord record) {
        return Boolean.TRUE.equals(record.getIsActive())
                && Boolean.FALSE.equals(record.getIsDeleted());
    }
}
