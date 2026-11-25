package com.example.demo.controller;

import com.example.demo.dto.TenantRequest;
import com.example.demo.entity.AppRole;
import com.example.demo.entity.Role;
import com.example.demo.entity.Tenant;
import com.example.demo.entity.UserAccount;
import com.example.demo.repository.TenantRepository;
import com.example.demo.repository.UserAccountRepository;
import com.example.demo.service.EmployeeAccountProvisioner;
import com.example.demo.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {
    private final TenantRepository tenants;
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public TenantController(
            TenantRepository tenants,
            UserAccountRepository users,
            PasswordEncoder passwordEncoder,
            RoleService roleService
    ) {
        this.tenants = tenants;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @GetMapping
    public List<Tenant> list() {
        return tenants.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> get(@PathVariable Long id) {
        return tenants.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Tenant create(@Valid @RequestBody TenantRequest request) {
        String name = request.name().trim();
        if (tenants.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tenant name already exists");
        }
        Tenant tenant = new Tenant();
        tenant.setName(name);
        tenant.setActive(request.isActive() == null || request.isActive());
        Tenant saved = tenants.save(tenant);
        Role adminRole = roleService.seedDefaultsForTenant(saved.getId());

        String username;
        String rawPassword;
        if (request.adminUsername() != null && !request.adminUsername().isBlank()) {
            if (request.adminPassword() == null || request.adminPassword().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "adminPassword is required when adminUsername is provided");
            }
            username = EmployeeAccountProvisioner.normalizeUsername(request.adminUsername());
            if (request.adminUsername().contains(":") || username.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid adminUsername");
            }
            rawPassword = request.adminPassword();
        } else {
            String base = EmployeeAccountProvisioner.normalizeUsername(name).replace(":", "");
            String suffix = "_" + saved.getId();
            int maxBase = Math.max(1, 100 - suffix.length());
            username = (base.length() > maxBase ? base.substring(0, maxBase) : base) + suffix;
            rawPassword = (request.adminPassword() != null && !request.adminPassword().isBlank())
                    ? request.adminPassword()
                    : username;
        }

        if (users.existsByNormalizedUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use");
        }
        UserAccount admin = new UserAccount();
        admin.setTenantId(saved.getId());
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        admin.setRole(AppRole.ADMIN);
        admin.setRoleId(adminRole.getId());
        admin.setEnabled(true);
        users.save(admin);
        saved.setAdminUsername(username);
        return saved;
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Tenant> update(@PathVariable Long id, @Valid @RequestBody TenantRequest request) {
        Tenant existing = tenants.findById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        String name = request.name().trim();
        if (!existing.getName().equalsIgnoreCase(name) && tenants.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tenant name already exists");
        }
        existing.setName(name);
        if (request.isActive() != null) {
            existing.setActive(request.isActive());
        }
        return ResponseEntity.ok(tenants.save(existing));
    }
}
