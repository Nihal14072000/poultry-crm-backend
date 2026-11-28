package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.dto.PartyActualOrderDto;
import com.example.demo.repository.OrderRepository;
import com.example.demo.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        return repository.findActiveInDateRange(TenantContext.requireTenantId(), startOfDay(startDate), startOfNextDay(endDate));
    }

    public List<Order> findActiveByPartyIdAndPartyType(Integer partyId, String partyType) {
        return repository.findActiveByPartyIdAndPartyType(TenantContext.requireTenantId(), partyId, partyType);
    }

    public BigDecimal sumActiveOrderWeightTimesRateByPartyIdAndPartyType(
            Integer partyId,
            String partyType
    ) {
        return repository.sumActiveOrderWeightTimesRateByPartyIdAndPartyType(TenantContext.requireTenantId(), partyId, partyType);
    }

    public List<Order> findActiveByPartyIdAndPartyType(
            Integer partyId,
            String partyType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return repository.findActiveByPartyIdAndPartyTypeInDateRange(
                TenantContext.requireTenantId(), partyId, partyType, startOfDay(startDate), startOfNextDay(endDate)
        );
    }

    public List<PartyActualOrderDto> findActualOrdersByPartyIdAndPartyType(Integer partyId, String partyType) {
        Long tenantId = TenantContext.requireTenantId();
        if (PartyOrderRelationService.BUYING_PARTY_TYPE.equals(partyType)) {
            return repository.findActualSupplierPickupOrders(tenantId, partyId);
        }
        if (PartyOrderRelationService.SELLING_PARTY_TYPE.equals(partyType)) {
            return repository.findActualVendorDeliveryOrders(tenantId, partyId);
        }
        throw new IllegalArgumentException("Party type must be B or S.");
    }

    public BigDecimal sumActualOrderWeightTimesRateByPartyIdAndPartyType(Integer partyId, String partyType) {
        return findActualOrdersByPartyIdAndPartyType(partyId, partyType).stream()
                .map(order -> (order.weight() == null ? BigDecimal.ZERO : order.weight())
                        .multiply(order.rate() == null ? BigDecimal.ZERO : order.rate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
        return repository.findActiveByTripIdAndPartyIdAndPartyType(TenantContext.requireTenantId(), tripId, partyId, partyType);
    }
}
