package com.example.demo.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TenantIdentifierResolverTest {
    private final TenantIdentifierResolver resolver = new TenantIdentifierResolver();

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void resolvesTenantFromRequestContext() {
        TenantContext.setTenantId("tenant-123");

        assertEquals("tenant-123", resolver.resolveCurrentTenantIdentifier());
    }

    @Test
    void usesDefaultTenantOnlyWhenNoAuthenticatedTenantExists() {
        assertEquals("default", resolver.resolveCurrentTenantIdentifier());
    }
}
