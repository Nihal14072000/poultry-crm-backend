package com.example.demo.repository;

import com.example.demo.entity.Expenses;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpensesRepository extends JpaRepository<Expenses, Integer> {
    List<Expenses> findByTripIdAndIsActiveTrueAndIsDeletedFalseOrderByExpenseDateDescIdDesc(Integer tripId);
}
