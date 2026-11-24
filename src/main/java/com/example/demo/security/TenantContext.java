package com.example.demo.security;

public final class TenantContext {
    private static final String DEFAULT_TENANT = "default";
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void setTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID is required");
        }
        CURRENT.set(tenantId);
    }

    public static String currentTenantId() {
        String tenantId = CURRENT.get();
        if (tenantId != null && !tenantId.isBlank()) return tenantId;
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserAccountPrincipal principal
                && principal.getTenantId() != null && !principal.getTenantId().isBlank()) {
            return principal.getTenantId();
        }
        return DEFAULT_TENANT;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
