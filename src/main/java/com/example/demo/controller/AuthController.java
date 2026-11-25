package com.example.demo.controller;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.EmployeeAccountResponse;
import com.example.demo.dto.SignupRequest;
import com.example.demo.entity.AppRole;
import com.example.demo.entity.Employee;
import com.example.demo.entity.Role;
import com.example.demo.entity.Tenant;
import com.example.demo.entity.UserAccount;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.TenantRepository;
import com.example.demo.security.TenantContext;
import com.example.demo.service.EmployeeAccountService;
import com.example.demo.service.EmployeeUserDetailsService;
import com.example.demo.service.RoleService;
import com.example.demo.service.SignupService;
import com.example.demo.repository.UserAccountRepository;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Locale;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Map<String, String> EMPLOYEE_ROLE_LABELS = Map.of(
            "DRIVER", "Driver",
            "SUPERVISOR", "Supervisor",
            "ACCOUNTANT", "Accountant",
            "LABOUR", "Labour"
    );

    private final SignupService signupService;
    private final EmployeeAccountService employeeAccountService;
    private final EmployeeUserDetailsService userDetailsService;
    private final UserAccountRepository accounts;
    private final EmployeeRepository employees;
    private final TenantRepository tenants;
    private final RoleService roleService;

    public AuthController(
            SignupService signupService,
            EmployeeAccountService employeeAccountService,
            EmployeeUserDetailsService userDetailsService,
            UserAccountRepository accounts,
            EmployeeRepository employees,
            TenantRepository tenants,
            RoleService roleService
    ) {
        this.signupService = signupService;
        this.employeeAccountService = employeeAccountService;
        this.userDetailsService = userDetailsService;
        this.accounts = accounts;
        this.employees = employees;
        this.tenants = tenants;
        this.roleService = roleService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeAccountResponse signup(@Valid @RequestBody SignupRequest request) {
        return signupService.signup(request);
    }

    @GetMapping("/me")
    public Map<String, Object> me(Principal principal) {
        if (userDetailsService.isSuperuser(principal.getName())) {
            return Map.of(
                    "id", "0",
                    "name", "Superuser",
                    "username", userDetailsService.getSuperuserUsername(),
                    "role", "Super Admin"
            );
        }
        var account = accounts.findByUsername(principal.getName()).orElseThrow();
        var employee = account.getEmployeeId() != null
                ? employees.findById(account.getEmployeeId()).orElse(null)
                : null;
        String resolvedId = employee != null ? employee.getId().toString() : account.getId().toString();
        String displayName = employee != null && employee.getName() != null && !employee.getName().isBlank()
                ? employee.getName()
                : account.getUsername();
        long tenantId = account.getTenantId() != null ? account.getTenantId() : TenantContext.DEFAULT_TENANT_ID;
        String tenantName = tenants.findById(tenantId).map(Tenant::getName).orElse("");
        Role dbRole = roleService.resolveRole(account, employee).orElse(null);
        if (dbRole != null) {
            return Map.of(
                    "id", resolvedId,
                    "name", displayName,
                    "username", account.getUsername(),
                    "tenantName", tenantName,
                    "role", dbRole.getName(),
                    "resources", dbRole.getResources(),
                    "actions", dbRole.getActions()
            );
        }
        return Map.of(
                "id", resolvedId,
                "name", displayName,
                "username", account.getUsername(),
                "tenantName", tenantName,
                "role", resolveRole(account, employee)
        );
    }

    private String resolveRole(UserAccount account, Employee employee) {
        if (account.getRole() == AppRole.ADMIN) {
            return "Organization Admin";
        }
        if (employee != null && employee.getType() != null) {
            String mapped = EMPLOYEE_ROLE_LABELS.get(employee.getType().trim().toUpperCase(Locale.ROOT));
            if (mapped != null) {
                return mapped;
            }
        }
        return "Authenticated";
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(Principal principal, @Valid @RequestBody ChangePasswordRequest request) {
        if (userDetailsService.isSuperuser(principal.getName())) {
            userDetailsService.changeSuperuserPassword(request.currentPassword(), request.newPassword());
            return;
        }
        employeeAccountService.changePassword(principal.getName(), request);
    }
}
