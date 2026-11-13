package com.example.demo.service;

import com.example.demo.entity.AppRole;
import com.example.demo.entity.Employee;
import com.example.demo.entity.UserAccount;
import com.example.demo.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.CompletableFuture;

@Service
public class EmployeeAccountProvisioner {
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeAccountProvisioner(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String validateNameForAccount(String employeeName) {
        if (employeeName == null || employeeName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee name is required");
        }

        String username = employeeName.trim();
        if (username.length() > 100 || username.contains(":")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee name must be at most 100 characters and must not contain ':'"
            );
        }
        if (userAccountRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account already uses this employee name; employee names used as usernames must be unique"
            );
        }
        return username;
    }

    @Async
    public void createAccount(Employee employee, String username) {
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(username));
        account.setRole(AppRole.EMPLOYEE);
        account.setEmployeeId(employee.getId());
        account.setEnabled(true);
        userAccountRepository.save(account);
    }

    public void deleteAccount(Integer employeeId) {
        userAccountRepository.deleteByEmployeeId(employeeId);
    }
}
