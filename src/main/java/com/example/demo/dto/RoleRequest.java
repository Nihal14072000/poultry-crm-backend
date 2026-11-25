package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public record RoleRequest(
        @NotBlank @Size(max = 100) String name,
        List<String> resources,
        Map<String, List<String>> actions
) {
}
