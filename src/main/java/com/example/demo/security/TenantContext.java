package com.example.demo.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

public final class TenantContext {
    public static final long DEFAULT_TENANT_ID = 1L;
    private static final ThreadLocal<Long> OVERRIDE = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(Long tenantId) {
        if (tenantId == null) {
            OVERRIDE.remove();
        } else {
            OVERRIDE.set(tenantId);
        }
    }

    public static void clear() {
        OVERRIDE.remove();
    }

    public static Long getTenantId() {
        Long override = OVERRIDE.get();
        if (override != null) {
            return override;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserAccountPrincipal principal) {
            return principal.getTenantId();
        }
        return null;
    }

    public static long requireTenantId() {
        Long tenantId = getTenantId();
        if (tenantId != null) {
            return tenantId;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return DEFAULT_TENANT_ID;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant context is missing");
    }

    public static long currentTenantIdOrDefault() {
        Long tenantId = getTenantId();
        return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
    }
}
