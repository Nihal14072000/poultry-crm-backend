package com.example.demo.service;

import com.example.demo.entity.AppRole;
import com.example.demo.entity.Employee;
import com.example.demo.entity.Role;
import com.example.demo.entity.UserAccount;
import com.example.demo.repository.RoleRepository;
import com.example.demo.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RoleService {
    private static final List<String> ADMIN_RESOURCES = List.of(
            "dashboard", "employees", "fleet", "vehicles", "trips", "finance",
            "payments", "expenses", "deliveries", "suppliers", "vendor-destinations", "users-roles"
    );

    private static final Map<String, List<String>> ADMIN_ACTIONS = Map.of(
            "create", ADMIN_RESOURCES,
            "update", ADMIN_RESOURCES,
            "delete", ADMIN_RESOURCES,
            "transition", ADMIN_RESOURCES,
            "approve", ADMIN_RESOURCES,
            "export", ADMIN_RESOURCES,
            "manage", ADMIN_RESOURCES
    );

    private final RoleRepository roles;

    public RoleService(RoleRepository roles) {
        this.roles = roles;
    }

    @Transactional
    public Role seedDefaultsForTenant(long tenantId) {
        Role adminRole = ensureRole(tenantId, "Organization Admin", ADMIN_RESOURCES, ADMIN_ACTIONS);
        ensureRole(tenantId, "Driver", List.of("trips", "deliveries"), Map.of());
        ensureRole(tenantId, "Supervisor", List.of("deliveries"),
                Map.of("update", List.of("deliveries"), "transition", List.of("deliveries")));
        ensureRole(tenantId, "Accountant",
                List.of("dashboard", "finance", "receivables", "payables", "payments", "expenses",
                        "profitability", "sales", "procurement", "reports", "analytics", "documents", "notifications", "audit"),
                Map.of("create", List.of("payments", "expenses", "receivables", "payables"),
                        "update", List.of("payments", "expenses", "receivables", "payables"),
                        "transition", List.of("payments", "expenses", "receivables", "payables"),
                        "approve", List.of("payments", "expenses")));
        ensureRole(tenantId, "Labour", List.of("deliveries"), Map.of());
        return adminRole;
    }

    public Optional<Role> resolveRole(UserAccount account, Employee employee) {
        long tenantId = account.getTenantId() != null ? account.getTenantId() : TenantContext.DEFAULT_TENANT_ID;
        if (account.getRoleId() != null) {
            Optional<Role> byId = roles.findByIdAndTenantId(account.getRoleId(), tenantId);
            if (byId.isPresent()) {
                return byId;
            }
        }
        if (account.getRole() == AppRole.ADMIN) {
            return roles.findByTenantIdAndNameIgnoreCase(tenantId, "Organization Admin");
        }
        if (employee != null && employee.getType() != null && !employee.getType().isBlank()) {
            return roles.findByTenantIdAndNameIgnoreCase(tenantId, employee.getType().trim());
        }
        return Optional.empty();
    }

    private Role ensureRole(long tenantId, String name, List<String> resources, Map<String, List<String>> actions) {
        return roles.findByTenantIdAndNameIgnoreCase(tenantId, name).orElseGet(() -> {
            Role role = new Role();
            role.setTenantId(tenantId);
            role.setName(name);
            role.setResources(List.copyOf(resources));
            role.setActions(new LinkedHashMap<>(actions));
            return roles.save(role);
        });
    }
}
