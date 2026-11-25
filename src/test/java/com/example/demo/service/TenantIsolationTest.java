package com.example.demo.service;

import com.example.demo.entity.Vehicle;
import com.example.demo.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TenantIsolationTest {

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void crudServiceFiltersAndBlocksCrossTenantAccess() {
        @SuppressWarnings("unchecked")
        JpaRepository<Vehicle, Integer> repo = mock(JpaRepository.class);
        CrudService<Vehicle> service = new CrudService<>(repo, Vehicle::setId);

        Vehicle t1 = new Vehicle();
        t1.setId(1);
        t1.setTenantId(1L);
        t1.setIsActive(true);
        t1.setIsDeleted(false);

        Vehicle t2 = new Vehicle();
        t2.setId(2);
        t2.setTenantId(2L);
        t2.setIsActive(true);
        t2.setIsDeleted(false);

        TenantContext.setTenantId(1L);
        when(repo.findAll(any(Sort.class))).thenReturn(List.of(t1, t2));
        when(repo.findById(2)).thenReturn(Optional.of(t2));

        assertEquals(List.of(t1), service.findAll());
        assertTrue(service.findById(2).isEmpty());
        assertTrue(service.update(2, new Vehicle()).isEmpty());
        assertFalse(service.delete(2));
        verify(repo, never()).deleteById(2);
    }
}
