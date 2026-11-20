package com.example.demo.service;

import com.example.demo.entity.AppRole;
import com.example.demo.entity.UserAccount;
import com.example.demo.entity.Vehicle;
import com.example.demo.repository.VehicleRepository;
import com.example.demo.security.UserAccountPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CrudServiceAuditTest {
    private final VehicleRepository repository = mock(VehicleRepository.class);
    private final CrudService<Vehicle> service = new CrudService<>(repository, Vehicle::setId);

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUsesCurrentTimeAndAuthenticatedUser() {
        authenticateAs(42);
        when(repository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Vehicle vehicle = new Vehicle();

        LocalDateTime beforeCreate = LocalDateTime.now();
        service.create(vehicle);

        assertNotNull(vehicle.getCreateDate());
        assertEquals(42, vehicle.getCreatedBy());
        assertNull(vehicle.getUpdatedDate());
        assertNull(vehicle.getUpdatedBy());
        assertFalse(vehicle.getCreateDate().isBefore(beforeCreate));
        verify(repository).save(vehicle);
    }

    @Test
    void updateKeepsCreationAuditAndSetsUpdaterAudit() {
        authenticateAs(84);
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        Vehicle existing = new Vehicle();
        existing.setCreateDate(createdAt);
        existing.setCreatedBy(42);
        when(repository.findById(7)).thenReturn(Optional.of(existing));
        when(repository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Vehicle updated = new Vehicle();

        service.update(7, updated);

        assertEquals(7, updated.getId());
        assertEquals(createdAt, updated.getCreateDate());
        assertEquals(42, updated.getCreatedBy());
        assertNotNull(updated.getUpdatedDate());
        assertEquals(84, updated.getUpdatedBy());
        verify(repository).save(updated);
    }

    private void authenticateAs(Integer id) {
        UserAccount account = new UserAccount();
        account.setId(id);
        account.setUsername("test-user");
        account.setPasswordHash("unused");
        account.setRole(AppRole.ADMIN);
        var principal = new UserAccountPrincipal(account);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities()));
    }
}
