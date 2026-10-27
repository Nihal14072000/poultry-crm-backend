package com.example.demo.service;

import com.example.demo.entity.AppRole;
import com.example.demo.entity.UserAccount;
import com.example.demo.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements ApplicationRunner {
    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminAccountInitializer(
            UserAccountRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.admin.username:admin}") String username,
            @Value("${app.security.admin.password:admin}") String password
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.existsByRole(AppRole.ADMIN)) {
            return;
        }
        if (repository.existsByUsername(username)) {
            throw new IllegalStateException("Configured bootstrap admin username already belongs to another account");
        }

        UserAccount admin = new UserAccount();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(AppRole.ADMIN);
        admin.setEnabled(true);
        repository.save(admin);
    }
}
