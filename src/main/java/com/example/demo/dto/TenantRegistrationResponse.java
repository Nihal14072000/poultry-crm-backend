package com.example.demo.dto;

public record TenantRegistrationResponse(
        String tenantId,
        String companyName,
        String username,
        String role
) {}
