package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public interface CreatedRecord {
    LocalDateTime getCreatedAtForSort();

    static <T extends CreatedRecord> List<T> newestFirst(List<T> records) {
        return records.stream()
                .sorted(Comparator.comparing(
                        CreatedRecord::getCreatedAtForSort,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
}
