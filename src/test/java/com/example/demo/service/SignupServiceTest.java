package com.example.demo.service;

import com.example.demo.dto.EmployeeAccountResponse;
import com.example.demo.dto.SignupRequest;
import com.example.demo.entity.Employee;
import com.example.demo.entity.UserAccount;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SignupService signupService;

    @Test
    void createsEmployeeAndLinkedEmployeeAccount() {
        when(userAccountRepository.existsByUsername("asha")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee employee = invocation.getArgument(0);
            employee.setId(42);
            return employee;
        });
        when(passwordEncoder.encode("a-long-unique-password")).thenReturn("password-hash");
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount account = invocation.getArgument(0);
            account.setId(17);
            return account;
        });

        EmployeeAccountResponse response = signupService.signup(new SignupRequest(
                " asha ",
                "a-long-unique-password",
                " Asha Kumar ",
                "Pune",
                "9876543210"
        ));

        assertEquals(new EmployeeAccountResponse(17, "asha", 42, "EMPLOYEE"), response);
        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(employeeCaptor.capture());
        assertEquals("Asha Kumar", employeeCaptor.getValue().getName());
        assertEquals("EMPLOYEE", employeeCaptor.getValue().getType());
        assertEquals("ACTIVE", employeeCaptor.getValue().getStatus());

        ArgumentCaptor<UserAccount> accountCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(accountCaptor.capture());
        assertEquals(42, accountCaptor.getValue().getEmployeeId());
        assertEquals("password-hash", accountCaptor.getValue().getPasswordHash());
        verify(passwordEncoder).encode("a-long-unique-password");
    }

    @Test
    void rejectsDuplicateUsernameWithoutCreatingEmployee() {
        when(userAccountRepository.existsByUsername("asha")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> signupService.signup(new SignupRequest(
                "asha",
                "a-long-unique-password",
                "Asha Kumar",
                null,
                null
        )));

        verify(employeeRepository, never()).save(any(Employee.class));
        verify(userAccountRepository, never()).save(any(UserAccount.class));
    }
}
