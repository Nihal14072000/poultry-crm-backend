package com.example.demo.repository;

import com.example.demo.entity.Expenses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ExpensesRepository extends JpaRepository<Expenses, Integer> {
    List<Expenses> findByTripIdAndIsActiveTrueAndIsDeletedFalseOrderByExpenseDateDescIdDesc(Integer tripId);

    @Modifying
    @Query("delete from Expenses e where e.tripId = :tripId")
    void deleteByTripId(@Param("tripId") Integer tripId);

    @Query("""
            select e from Expenses e
            where e.tenantId = :tenantId
              and e.isActive = true
              and e.isDeleted = false
              and (:tripId is null or e.tripId = :tripId)
              and (:q is null or lower(coalesce(e.category, '')) like :q
                   or lower(coalesce(e.note, '')) like :q)
            """)
    Page<Expenses> findPage(
            @Param("tenantId") Long tenantId,
            @Param("tripId") Integer tripId,
            @Param("q") String q,
            Pageable pageable
    );

    @Query("""
            select coalesce(sum(coalesce(e.amount, 0)), 0) from Expenses e
            where e.tenantId = :tenantId
              and e.isActive = true
              and e.isDeleted = false
              and (:tripId is null or e.tripId = :tripId)
              and (:q is null or lower(coalesce(e.category, '')) like :q
                   or lower(coalesce(e.note, '')) like :q)
            """)
    BigDecimal sumAmount(
            @Param("tenantId") Long tenantId,
            @Param("tripId") Integer tripId,
            @Param("q") String q
    );
}
