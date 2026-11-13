package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(min = 12, max = 72) String password,
        @NotBlank @Size(max = 250) String name,
        @Size(max = 250) String address,
        @Size(max = 250) String number
) {
}
