package com.example.demo.service;

import com.example.demo.entity.AuditableRecord;
import com.example.demo.entity.TimestampedRecord;
import com.example.demo.security.UserAccountPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

final class AuditSupport {
    private AuditSupport() {
    }

    static void markCreated(TimestampedRecord record) {
        record.setCreateDate(LocalDateTime.now());
        record.setUpdatedDate(null);
        if (record instanceof AuditableRecord auditable) {
            auditable.setCreatedBy(currentUserId());
            auditable.setUpdatedBy(null);
        }
    }

    static void markUpdated(TimestampedRecord record, TimestampedRecord existing) {
        record.setCreateDate(existing.getCreatedAtForSort());
        record.setUpdatedDate(LocalDateTime.now());
        if (record instanceof AuditableRecord auditable) {
            if (existing instanceof AuditableRecord existingAuditable) {
                auditable.setCreatedBy(existingAuditable.getCreatedBy());
            }
            auditable.setUpdatedBy(currentUserId());
        }
    }

    private static Integer currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserAccountPrincipal principal)) {
            return null;
        }
        return principal.getId();
    }
}
