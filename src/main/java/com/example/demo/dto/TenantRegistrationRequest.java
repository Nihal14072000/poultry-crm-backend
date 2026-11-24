package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TenantRegistrationRequest(
        @NotBlank @Size(max = 150) String companyName,
        @NotBlank @Size(max = 250) String ownerName,
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(min = 12, max = 72) String password
) {}
