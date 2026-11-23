package com.example.demo.service;

import com.example.demo.entity.Expenses;
import com.example.demo.repository.ExpensesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExpensesService extends CrudService<Expenses> {
    private final ExpensesRepository repository;

    public ExpensesService(ExpensesRepository repository) {
        super(repository, Expenses::setId);
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Expenses> findByTripId(Integer tripId) {
        if (tripId == null || tripId <= 0) {
            throw new IllegalArgumentException("Trip ID must be a positive integer.");
        }
        return repository.findByTripIdAndIsActiveTrueAndIsDeletedFalseOrderByExpenseDateDescIdDesc(tripId);
    }

    @Transactional
    public void deleteByTripId(Integer tripId) {
        repository.deleteByTripId(tripId);
    }
}
