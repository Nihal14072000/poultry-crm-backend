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
        long tenantId = com.example.demo.security.TenantContext.requireTenantId();
        return repository.findByTripIdAndIsActiveTrueAndIsDeletedFalseOrderByExpenseDateDescIdDesc(tripId).stream()
                .filter(e -> java.util.Objects.equals(e.getTenantId(), tenantId))
                .toList();
    }

    @Transactional
    public void deleteByTripId(Integer tripId) {
        repository.deleteByTripId(tripId);
    }
}
