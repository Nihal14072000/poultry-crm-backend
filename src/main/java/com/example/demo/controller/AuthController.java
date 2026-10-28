package com.example.demo.controller;

import com.example.demo.dto.EmployeeAccountResponse;
import com.example.demo.dto.SignupRequest;
import com.example.demo.service.SignupService;
import com.example.demo.repository.UserAccountRepository;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final SignupService signupService;
    private final UserAccountRepository accounts;

    public AuthController(SignupService signupService, UserAccountRepository accounts) {
        this.signupService = signupService;
        this.accounts = accounts;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeAccountResponse signup(@Valid @RequestBody SignupRequest request) {
        return signupService.signup(request);
    }

    @GetMapping("/me")
    public Map<String, String> me(Principal principal) {
        var account = accounts.findByUsername(principal.getName()).orElseThrow();
        return Map.of("name", account.getUsername(), "role", account.getRole().name());
    }
}
