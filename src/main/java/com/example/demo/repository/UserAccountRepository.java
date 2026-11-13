package com.example.demo.repository;

import com.example.demo.entity.AppRole;
import com.example.demo.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {
    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmployeeId(Integer employeeId);

    boolean existsByRole(AppRole role);

    void deleteByEmployeeId(Integer employeeId);
}
