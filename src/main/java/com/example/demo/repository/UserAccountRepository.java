package com.example.demo.repository;

import com.example.demo.entity.AppRole;
import com.example.demo.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {
    Optional<UserAccount> findByUsername(String username);

    @Query("SELECT u FROM UserAccount u WHERE LOWER(REPLACE(u.username, ' ', '')) = :username")
    Optional<UserAccount> findByNormalizedUsername(@Param("username") String username);

    boolean existsByUsername(String username);

    @Query("SELECT COUNT(u) > 0 FROM UserAccount u WHERE LOWER(REPLACE(u.username, ' ', '')) = :username")
    boolean existsByNormalizedUsername(@Param("username") String username);

    boolean existsByEmployeeId(Integer employeeId);

    boolean existsByRole(AppRole role);

    boolean existsByRoleAndTenantId(AppRole role, Long tenantId);

    long countByRoleId(Long roleId);

    void deleteByEmployeeId(Integer employeeId);
}
