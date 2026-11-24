package com.example.demo.security;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
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
