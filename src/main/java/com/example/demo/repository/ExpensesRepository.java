package com.example.demo.repository;

import com.example.demo.entity.Expenses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpensesRepository extends JpaRepository<Expenses, Integer> {
    List<Expenses> findByTripIdAndIsActiveTrueAndIsDeletedFalseOrderByExpenseDateDescIdDesc(Integer tripId);

    @Modifying
    @Query("delete from Expenses e where e.tripId = :tripId")
    void deleteByTripId(@Param("tripId") Integer tripId);
}
