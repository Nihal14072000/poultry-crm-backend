package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.PartyOrderRelation;
import com.example.demo.repository.PartyOrderRelationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PartyOrderRelationService {
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
    public void replaceOrders(Integer partyId, List<Order> orders, String partyType) {
        if (orders == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<PartyOrderRelation> existingRelations =
                repository.findByPartyIdAndPartyType(partyId, partyType);
        existingRelations.stream()
                .filter(this::isActive)
                .forEach(relation -> deactivate(relation, now));
        repository.saveAll(existingRelations);

        List<PartyOrderRelation> newRelations = new ArrayList<>();
        for (Order requestedOrder : orders) {
            Order order = requestedOrder.getId() == null
                    ? orderService.create(requestedOrder)
                    : orderService.findById(requestedOrder.getId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Order not found: " + requestedOrder.getId()));
            PartyOrderRelation relation = new PartyOrderRelation();
            relation.setPartyId(partyId);
            relation.setOrderId(order.getId());
            relation.setPartyType(partyType);
            relation.setIsActive(true);
            relation.setIsDeleted(false);
            relation.setCreateDate(now);
            newRelations.add(relation);
        }
        repository.saveAll(newRelations);
    }

    @Transactional(readOnly = true)
    public List<Order> findActiveOrders(Integer partyId, String partyType) {
        return repository.findByPartyIdAndPartyType(partyId, partyType).stream()
                .filter(this::isActive)
                .map(relation -> orderService.findById(relation.getOrderId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Order not found for party-order relation " + relation.getRelationId())))
                .toList();
    }

    private void deactivate(PartyOrderRelation relation, LocalDateTime now) {
        relation.setIsActive(false);
        relation.setIsDeleted(true);
        relation.setUpdatedDate(now);
    }

    private boolean isActive(PartyOrderRelation relation) {
        return !Boolean.TRUE.equals(relation.getIsDeleted())
                && !Boolean.FALSE.equals(relation.getIsActive());
    }
}
