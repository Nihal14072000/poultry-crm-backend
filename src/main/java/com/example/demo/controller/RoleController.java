package com.example.demo.controller;

import com.example.demo.dto.RoleRequest;
import com.example.demo.entity.Role;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserAccountRepository;
import com.example.demo.security.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleRepository roles;
    private final UserAccountRepository users;

    public RoleController(RoleRepository roles, UserAccountRepository users) {
        this.roles = roles;
        this.users = users;
    }

    @GetMapping
    public List<Role> list() {
        return roles.findByTenantIdOrderByIdAsc(TenantContext.requireTenantId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Role create(@Valid @RequestBody RoleRequest request) {
        long tenantId = TenantContext.requireTenantId();
        String name = request.name().trim();
        if (roles.existsByTenantIdAndNameIgnoreCase(tenantId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role name already exists for this tenant");
        }
        Role role = new Role();
        role.setTenantId(tenantId);
        role.setName(name);
        role.setResources(sanitizeResources(request.resources()));
        role.setActions(sanitizeActions(request.actions()));
        return roles.save(role);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Role> update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        long tenantId = TenantContext.requireTenantId();
        Role existing = roles.findByIdAndTenantId(id, tenantId).orElse(null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        String name = request.name().trim();
        if (!existing.getName().equalsIgnoreCase(name)
                && roles.existsByTenantIdAndNameIgnoreCase(tenantId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role name already exists for this tenant");
        }
        existing.setName(name);
        existing.setResources(sanitizeResources(request.resources()));
        existing.setActions(sanitizeActions(request.actions()));
        return ResponseEntity.ok(roles.save(existing));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        long tenantId = TenantContext.requireTenantId();
        Role existing = roles.findByIdAndTenantId(id, tenantId).orElse(null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        if (users.countByRoleId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role is assigned to existing user accounts");
        }
        roles.delete(existing);
        return ResponseEntity.noContent().build();
    }

    private static List<String> sanitizeResources(List<String> resources) {
        return resources == null ? List.of() : resources.stream().distinct().toList();
    }

    private static Map<String, List<String>> sanitizeActions(Map<String, List<String>> actions) {
        if (actions == null) {
            return Map.of();
        }
        Map<String, List<String>> cleaned = new LinkedHashMap<>();
        actions.forEach((action, list) -> {
            if (list != null && !list.isEmpty()) {
                cleaned.put(action, list.stream().distinct().toList());
            }
        });
        return cleaned;
    }
}
