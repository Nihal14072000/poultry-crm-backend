package com.example.demo.repository;

import com.example.demo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByTenantIdOrderByIdAsc(Long tenantId);

    Optional<Role> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Role> findByTenantIdAndNameIgnoreCase(Long tenantId, String name);

    boolean existsByTenantIdAndNameIgnoreCase(Long tenantId, String name);
}
