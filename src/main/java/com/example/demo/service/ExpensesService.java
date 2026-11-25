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
    public org.springframework.data.domain.Page<Expenses> findPage(
            int page, int size, Integer tripId, String query) {
        var pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Order.desc("expenseDate").nullsLast(),
                        org.springframework.data.domain.Sort.Order.desc("id")));
        String q = (query == null || query.isBlank()) ? null : "%" + query.trim().toLowerCase() + "%";
        return repository.findPage(
                com.example.demo.security.TenantContext.requireTenantId(), tripId, q, pageable);
    }

    @Transactional(readOnly = true)
    public java.math.BigDecimal sumAmount(Integer tripId, String query) {
        String q = (query == null || query.isBlank()) ? null : "%" + query.trim().toLowerCase() + "%";
        return repository.sumAmount(
                com.example.demo.security.TenantContext.requireTenantId(), tripId, q);
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
