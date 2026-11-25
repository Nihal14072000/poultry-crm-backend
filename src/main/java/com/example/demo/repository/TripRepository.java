package com.example.demo.repository;

import com.example.demo.entity.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripRepository extends JpaRepository<Trip, Integer> {
    @Query("""
            select t from Trip t
            where t.tenantId = :tenantId
              and (:driverId is null or t.driverId = :driverId)
              and (:supervisorId is null or t.supervisorId = :supervisorId)
            """)
    Page<Trip> findPage(
            @Param("tenantId") Long tenantId,
            @Param("driverId") Integer driverId,
            @Param("supervisorId") Integer supervisorId,
            Pageable pageable
    );
}
