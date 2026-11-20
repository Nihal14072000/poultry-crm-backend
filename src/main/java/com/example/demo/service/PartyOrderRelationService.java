package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.PartyOrderRelation;
import com.example.demo.repository.PartyOrderRelationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PartyOrderRelationService {
    public static final String BUYING_PARTY_TYPE = "B";
    public static final String SELLING_PARTY_TYPE = "S";
    public static final String SUPPLIER_PICKUP_PARTY_TYPE = "SUPPLIER_PICKUP";
    public static final String VENDOR_DELIVERY_PARTY_TYPE = "VENDOR_DELIVERY";

    private final PartyOrderRelationRepository repository;
    private final OrderService orderService;

    public PartyOrderRelationService(
            PartyOrderRelationRepository repository,
            OrderService orderService
    ) {
        this.repository = repository;
        this.orderService = orderService;
    }

    @Transactional
    public void deleteOrdersByTripId(Integer tripId) {
        orderService.deleteByTripId(tripId);
    }

    @Transactional
    public void replaceOrders(Integer partyId, List<Order> orders, String partyType) {
        replaceOrders(partyId, orders, partyType, null);
    }

    @Transactional
    public void replaceOrders(Integer partyId, List<Order> orders, String partyType, Integer tripId) {
        if (orders == null) {
            return;
        }

        List<PartyOrderRelation> existingRelations = findRelationsForReplacement(partyId, partyType, tripId);
        existingRelations.stream()
                .filter(this::isActive)
                .forEach(this::deactivate);
        repository.saveAll(existingRelations);

        List<PartyOrderRelation> newRelations = new ArrayList<>();
        for (Order requestedOrder : orders) {
            Order order = saveOrderForTrip(requestedOrder, tripId);
            PartyOrderRelation relation = new PartyOrderRelation();
            relation.setPartyId(partyId);
            relation.setOrderId(order.getId());
            relation.setPartyType(partyType);
            relation.setIsActive(true);
            relation.setIsDeleted(false);
            AuditSupport.markCreated(relation);
            newRelations.add(relation);
        }
        repository.saveAll(newRelations);
    }

    @Transactional
    public void addOrders(Integer partyId, List<Order> orders, String partyType) {
        addOrders(partyId, orders, partyType, null);
    }

    @Transactional
    public void addOrders(Integer partyId, List<Order> orders, String partyType, Integer tripId) {
        if (orders == null) {
            return;
        }

        Set<Integer> activeOrderIds = findRelationsForReplacement(partyId, partyType, tripId).stream()
                .filter(this::isActive)
                .map(PartyOrderRelation::getOrderId)
                .collect(Collectors.toSet());
        List<PartyOrderRelation> newRelations = new ArrayList<>();
        for (Order requestedOrder : orders) {
            Order order = saveOrderForTrip(requestedOrder, tripId);
            if (!activeOrderIds.add(order.getId())) {
                continue;
            }

            PartyOrderRelation relation = new PartyOrderRelation();
            relation.setPartyId(partyId);
            relation.setOrderId(order.getId());
            relation.setPartyType(partyType);
            relation.setIsActive(true);
            relation.setIsDeleted(false);
            AuditSupport.markCreated(relation);
            newRelations.add(relation);
        }
        if (!newRelations.isEmpty()) {
            repository.saveAll(newRelations);
        }
    }

    @Transactional(readOnly = true)
    public List<Order> findActiveOrders(Integer partyId, String partyType) {
        return orderService.findActiveByPartyIdAndPartyType(partyId, partyType);
    }

    @Transactional(readOnly = true)
    public List<Order> findActiveOrders(Integer tripId, Integer partyId, String partyType) {
        return orderService.findActiveByTripIdAndPartyIdAndPartyType(tripId, partyId, partyType);
    }

    private List<PartyOrderRelation> findRelationsForReplacement(
            Integer partyId,
            String partyType,
            Integer tripId
    ) {
        // Trip orders must be isolated by trip. A null tripId is reserved for
        // actual pickup/delivery records, whose party IDs are their record IDs.
        if (tripId == null) {
            return repository.findByPartyIdAndPartyType(partyId, partyType);
        }
        return repository.findByPartyIdAndPartyTypeAndTripId(partyId, partyType, tripId);
    }

    private Order saveOrderForTrip(Order requestedOrder, Integer tripId) {
        if (requestedOrder.getId() == null) {
            if (tripId != null) {
                requestedOrder.setTripId(tripId);
            }
            return orderService.create(requestedOrder);
        }

        Order order = orderService.findById(requestedOrder.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + requestedOrder.getId()));
        if (tripId == null) {
            return order;
        }
        if (order.getTripId() != null && !order.getTripId().equals(tripId)) {
            throw new IllegalArgumentException(
                    "Order " + order.getId() + " is already assigned to trip " + order.getTripId());
        }
        order.setTripId(tripId);
        return orderService.update(order.getId(), order)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + order.getId()));
    }

    private void deactivate(PartyOrderRelation relation) {
        relation.setIsActive(false);
        relation.setIsDeleted(true);
        AuditSupport.markUpdated(relation, relation);
    }

    private boolean isActive(PartyOrderRelation relation) {
        return Boolean.TRUE.equals(relation.getIsActive())
                && Boolean.FALSE.equals(relation.getIsDeleted());
    }
}
