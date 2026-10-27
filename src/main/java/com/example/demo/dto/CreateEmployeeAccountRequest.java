package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEmployeeAccountRequest(
        @NotNull Integer employeeId,
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(min = 12, max = 72) String password
) {
}
