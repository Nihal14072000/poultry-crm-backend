package com.example.demo.controller;

import com.example.demo.dto.CreateEmployeeAccountRequest;
import com.example.demo.dto.EmployeeAccountResponse;
import com.example.demo.service.EmployeeAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final EmployeeAccountService service;

    public AdminUserController(EmployeeAccountService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeAccountResponse createEmployeeAccount(
            @Valid @RequestBody CreateEmployeeAccountRequest request
    ) {
        return service.create(request);
    }
}
