package com.example.demo.service;

import com.example.demo.entity.AppRole;
import com.example.demo.entity.UserAccount;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeLoginNormalizationTest {
    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeAccountProvisioner provisioner;

    private EmployeeUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode("superuser")).thenReturn("super-hash");
        userDetailsService = new EmployeeUserDetailsService(
                userAccountRepository,
                employeeRepository,
                passwordEncoder,
                "superuser",
                "superuser"
        );
    }

    @Test
    void validateNameForAccountStripsAllSpacesAndLowercases() {
        when(userAccountRepository.existsByNormalizedUsername("johndoe")).thenReturn(false);

        String username = provisioner.validateNameForAccount("  John   Doe ");

        assertEquals("johndoe", username);
        verify(userAccountRepository).existsByNormalizedUsername("johndoe");
    }

    @Test
    void validateNameForAccountRejectsCaseAndSpaceDuplicate() {
        when(userAccountRepository.existsByNormalizedUsername("johndoe")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> provisioner.validateNameForAccount("JOHN DOE"));
    }

    @Test
    void loadUserByUsernameIgnoresSpacesAndCase() {
        UserAccount account = new UserAccount();
        account.setId(5);
        account.setUsername("johndoe");
        account.setPasswordHash("hash");
        account.setRole(AppRole.EMPLOYEE);
        account.setEnabled(true);

        when(userAccountRepository.findByNormalizedUsername("johndoe")).thenReturn(Optional.of(account));

        UserDetails details = userDetailsService.loadUserByUsername(" John  Doe ");

        assertEquals("johndoe", details.getUsername());
        verify(userAccountRepository).findByNormalizedUsername("johndoe");
    }

    @Test
    void loadUserByUsernameRejectsBlankInput() {
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("   "));
    }
}
