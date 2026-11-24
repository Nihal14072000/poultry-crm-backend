package com.example.demo.service;

import com.example.demo.dto.TenantRegistrationRequest;
import com.example.demo.dto.TenantRegistrationResponse;
import com.example.demo.entity.AppRole;
import com.example.demo.entity.Tenant;
import com.example.demo.entity.UserAccount;
import com.example.demo.repository.TenantRepository;
import com.example.demo.repository.UserAccountRepository;
import com.example.demo.security.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class TenantRegistrationService {
    private final TenantRepository tenants;
    private final UserAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactions;

    public TenantRegistrationService(
            TenantRepository tenants,
            UserAccountRepository accounts,
            PasswordEncoder passwordEncoder,
            PlatformTransactionManager transactionManager
    ) {
        this.tenants = tenants;
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    public TenantRegistrationResponse register(TenantRegistrationRequest request) {
        String username = request.username().trim();
        if (username.contains(":")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must not contain ':'");
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes");
        }
        if (accounts.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use");
        }

        String tenantId = UUID.randomUUID().toString();
        TenantContext.setTenantId(tenantId);
        try {
            return transactions.execute(status -> {
                Tenant tenant = new Tenant();
                tenant.setId(tenantId);
                tenant.setCompanyName(request.companyName().trim());
                tenant.setOwnerName(request.ownerName().trim());
                String baseSlug = request.companyName().toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
                tenant.setSlug((baseSlug.isBlank() ? "organization" : baseSlug) + "-" + tenantId.substring(0, 8));
                tenant.setCreatedAt(LocalDateTime.now());
                tenants.save(tenant);

                UserAccount admin = new UserAccount();
                admin.setUsername(username);
                admin.setPasswordHash(passwordEncoder.encode(request.password()));
                admin.setRole(AppRole.ADMIN);
                admin.setTenantId(tenantId);
                admin.setEnabled(true);
                accounts.save(admin);

                return new TenantRegistrationResponse(tenantId, tenant.getCompanyName(), username, AppRole.ADMIN.name());
            });
        } finally {
            TenantContext.clear();
        }
    }
}
