package com.example.demo.service;

import com.example.demo.dto.CreateEmployeeAccountRequest;
import com.example.demo.dto.EmployeeAccountResponse;
import com.example.demo.entity.AppRole;
import com.example.demo.entity.UserAccount;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmployeeAccountService {
    private final UserAccountRepository userAccountRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeAccountService(
            UserAccountRepository userAccountRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public EmployeeAccountResponse create(CreateEmployeeAccountRequest request) {
        String username = request.username().trim();
        if (userAccountRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use");
        }
        if (!employeeRepository.existsById(request.employeeId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee does not exist");
        }
        if (userAccountRepository.existsByEmployeeId(request.employeeId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee already has an account");
        }

        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setRole(AppRole.EMPLOYEE);
        account.setEmployeeId(request.employeeId());
        UserAccount saved = userAccountRepository.save(account);
        return new EmployeeAccountResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getEmployeeId(),
                saved.getRole().name()
        );
    }
}
