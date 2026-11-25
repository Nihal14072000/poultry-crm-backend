package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TenantRequest(
        @NotBlank @Size(max = 150) String name,
        Boolean isActive,
        @Size(max = 100) String adminUsername,
        @Size(min = 6, max = 72) String adminPassword
) {
}
