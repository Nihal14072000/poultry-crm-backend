package com.example.demo.service;

import com.example.demo.entity.Employee;
import com.example.demo.entity.Tenant;
import com.example.demo.entity.UserAccount;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.TenantRepository;
import com.example.demo.repository.UserAccountRepository;
import com.example.demo.security.TenantContext;
import com.example.demo.security.UserAccountPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
public class EmployeeUserDetailsService implements UserDetailsService {
    private final UserAccountRepository repository;
    private final EmployeeRepository employeeRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final String superuserUsername;
    private volatile String superuserPasswordHash;

    public EmployeeUserDetailsService(
            UserAccountRepository repository,
            EmployeeRepository employeeRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.superuser.username:superuser}") String superuserUsername,
            @Value("${app.security.superuser.password:superuser}") String superuserPassword
    ) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.superuserUsername = EmployeeAccountProvisioner.normalizeUsername(superuserUsername);
        this.superuserPasswordHash = passwordEncoder.encode(superuserPassword);
    }

    public boolean isSuperuser(String username) {
        String normalized = EmployeeAccountProvisioner.normalizeUsername(username);
        return !superuserUsername.isEmpty() && superuserUsername.equals(normalized);
    }

    public String getSuperuserUsername() {
        return superuserUsername;
    }

    public synchronized void changeSuperuserPassword(String currentPassword, String newPassword) {
        if (newPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes");
        }
        if (!passwordEncoder.matches(currentPassword, this.superuserPasswordHash)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        this.superuserPasswordHash = passwordEncoder.encode(newPassword);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalized = EmployeeAccountProvisioner.normalizeUsername(username);
        if (normalized.isEmpty()) {
            throw new UsernameNotFoundException("Account not found");
        }
        if (isSuperuser(normalized)) {
            return User.withUsername(superuserUsername)
                    .password(superuserPasswordHash)
                    .roles("ADMIN", "SUPERUSER")
                    .build();
        }
        UserAccount account = repository.findByNormalizedUsername(normalized)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));

        long tenantId = account.getTenantId() != null ? account.getTenantId() : TenantContext.DEFAULT_TENANT_ID;
        boolean tenantActive = tenantRepository.findById(tenantId)
                .map(Tenant::isActive)
                .orElse(true);

        String employeeType = account.getEmployeeId() != null
                ? employeeRepository.findById(account.getEmployeeId()).map(Employee::getType).orElse(null)
                : null;
        return new UserAccountPrincipal(account, employeeType, tenantActive);
    }
}
