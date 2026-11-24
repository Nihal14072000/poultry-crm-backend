package com.example.demo.controller;

import com.example.demo.dto.TenantRegistrationRequest;
import com.example.demo.dto.TenantRegistrationResponse;
import com.example.demo.service.TenantRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenants")
public class TenantRegistrationController {
    private final TenantRegistrationService service;

    public TenantRegistrationController(TenantRegistrationService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TenantRegistrationResponse register(@Valid @RequestBody TenantRegistrationRequest request) {
        return service.register(request);
    }
}
