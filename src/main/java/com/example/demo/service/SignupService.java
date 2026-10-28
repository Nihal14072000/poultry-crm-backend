package com.example.demo.service;

import com.example.demo.dto.EmployeeAccountResponse;
import com.example.demo.dto.SignupRequest;
import com.example.demo.entity.AppRole;
import com.example.demo.entity.Employee;
import com.example.demo.entity.UserAccount;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
public class SignupService {
    private final EmployeeRepository employeeRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupService(
            EmployeeRepository employeeRepository,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.employeeRepository = employeeRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public EmployeeAccountResponse signup(SignupRequest request) {
        String username = request.username().trim();
        if (username.contains(":")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must not contain ':'");
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes");
        }
        if (userAccountRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use");
        }

        Employee employee = new Employee();
        employee.setName(request.name().trim());
        employee.setAddress(request.address());
        employee.setNumber(request.number());
        employee.setType("EMPLOYEE");
        employee.setStatus("ACTIVE");
        Employee savedEmployee = employeeRepository.save(employee);

        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setRole(AppRole.EMPLOYEE);
        account.setEmployeeId(savedEmployee.getId());
        UserAccount savedAccount = userAccountRepository.save(account);

        return new EmployeeAccountResponse(
                savedAccount.getId(),
                savedAccount.getUsername(),
                savedAccount.getEmployeeId(),
                savedAccount.getRole().name()
        );
    }
}
