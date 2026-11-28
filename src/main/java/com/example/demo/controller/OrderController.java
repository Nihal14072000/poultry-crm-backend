package com.example.demo.controller;

import com.example.demo.entity.Order;
import com.example.demo.dto.PartyActualOrderDto;
import com.example.demo.service.PartyOrderRelationService;
import com.example.demo.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;
    private final PartyOrderRelationService partyOrderRelationService;

    public OrderController(OrderService service, PartyOrderRelationService partyOrderRelationService) {
        this.service = service;
        this.partyOrderRelationService = partyOrderRelationService;
    }

    @GetMapping
    public ResponseEntity<List<Order>> findAll(
            @RequestParam(required = false) Integer partyId,
            @RequestParam(required = false) String partyType,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }
        if (partyId == null && partyType == null) {
            return ResponseEntity.ok(service.findAll(startDate, endDate));
        }
        if (partyId == null || partyId <= 0
                || !(PartyOrderRelationService.BUYING_PARTY_TYPE.equals(partyType)
                || PartyOrderRelationService.SELLING_PARTY_TYPE.equals(partyType))) {
            return ResponseEntity.badRequest().build();
        }
        if (startDate == null && endDate == null) {
            return ResponseEntity.ok(partyOrderRelationService.findActiveOrders(partyId, partyType));
        }
        return ResponseEntity.ok(service.findActiveByPartyIdAndPartyType(
                partyId, partyType, startDate, endDate
        ));
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getPartyOrderTotal(
            @RequestParam Integer partyId,
            @RequestParam String partyType
    ) {
        if (partyId <= 0 || partyType.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(
                service.sumActiveOrderWeightTimesRateByPartyIdAndPartyType(partyId, partyType).setScale(0, RoundingMode.HALF_UP)
        );
    }

    @GetMapping("/actual")
    public ResponseEntity<List<PartyActualOrderDto>> findActualOrders(
            @RequestParam Integer partyId,
            @RequestParam String partyType
    ) {
        if (partyId == null || partyId <= 0
                || !(PartyOrderRelationService.BUYING_PARTY_TYPE.equals(partyType)
                || PartyOrderRelationService.SELLING_PARTY_TYPE.equals(partyType))) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.findActualOrdersByPartyIdAndPartyType(partyId, partyType));
    }

    @GetMapping("/actual/total")
    public ResponseEntity<BigDecimal> getActualPartyOrderTotal(
            @RequestParam Integer partyId,
            @RequestParam String partyType
    ) {
        if (partyId == null || partyId <= 0
                || !(PartyOrderRelationService.BUYING_PARTY_TYPE.equals(partyType)
                || PartyOrderRelationService.SELLING_PARTY_TYPE.equals(partyType))) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.sumActualOrderWeightTimesRateByPartyIdAndPartyType(
                partyId, partyType).setScale(0, RoundingMode.HALF_UP));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(order));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> update(@PathVariable Integer id, @RequestBody Order order) {
        return service.update(id, order)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!service.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
