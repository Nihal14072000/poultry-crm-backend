package com.example.demo.security;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {
    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.currentTenantId();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
