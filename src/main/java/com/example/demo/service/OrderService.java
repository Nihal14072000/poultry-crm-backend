package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService extends CrudService<Order> {
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        super(repository, Order::setId);
        this.repository = repository;
    }

    @Transactional
    public void deleteByTripId(Integer tripId) {
        repository.deleteByTripId(tripId);
    }

    public List<Order> findAll(LocalDate startDate, LocalDate endDate) {
        return repository.findActiveInDateRange(startOfDay(startDate), startOfNextDay(endDate));
    }

    public List<Order> findActiveByPartyIdAndPartyType(Integer partyId, String partyType) {
        return repository.findActiveByPartyIdAndPartyType(partyId, partyType);
    }

    public List<Order> findActiveByPartyIdAndPartyType(
            Integer partyId,
            String partyType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return repository.findActiveByPartyIdAndPartyTypeInDateRange(
                partyId, partyType, startOfDay(startDate), startOfNextDay(endDate)
        );
    }

    private LocalDateTime startOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }

    private LocalDateTime startOfNextDay(LocalDate date) {
        return date == null ? null : date.plusDays(1).atStartOfDay();
    }

    public List<Order> findActiveByTripIdAndPartyIdAndPartyType(
            Integer tripId,
            Integer partyId,
            String partyType
    ) {
        return repository.findActiveByTripIdAndPartyIdAndPartyType(tripId, partyId, partyType);
    }
}
