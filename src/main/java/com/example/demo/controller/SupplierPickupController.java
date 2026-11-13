package com.example.demo.controller;

import com.example.demo.entity.SupplierPickup;
import com.example.demo.entity.Order;
import com.example.demo.dto.SupplierPickupRetrieveResponse;
import com.example.demo.service.PartyOrderRelationService;
import com.example.demo.service.SupplierPickupService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/supplier-pickups")
public class SupplierPickupController {
    private final SupplierPickupService service;
    private final PartyOrderRelationService partyOrderRelationService;

    public SupplierPickupController(
            SupplierPickupService service,
            PartyOrderRelationService partyOrderRelationService
    ) {
        this.service = service;
        this.partyOrderRelationService = partyOrderRelationService;
    }

    @GetMapping
    public List<SupplierPickup> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierPickup> findById(@PathVariable Integer id) {
        return service.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/retrieve")
    @Operation(
            summary = "Retrieve supplier pickup details",
            description = "Returns the supplier pickup with its buying party, actual order, and related orders. Returns 404 if it does not exist."
    )
    public ResponseEntity<SupplierPickupRetrieveResponse> retrieve(@PathVariable Integer id) {
        return service.retrieve(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SupplierPickupRetrieveResponse> create(@RequestBody SupplierPickup pickup) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAndRetrieve(pickup));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierPickup> update(@PathVariable Integer id, @RequestBody SupplierPickup pickup) {
        return service.update(id, pickup).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<List<Order>> findOrders(@PathVariable Integer id) {
        if (service.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(partyOrderRelationService.findActiveOrders(
                id, PartyOrderRelationService.SUPPLIER_PICKUP_PARTY_TYPE));
    }

    @PutMapping("/{id}/orders")
    public ResponseEntity<List<Order>> replaceOrders(
            @PathVariable Integer id,
            @RequestBody List<Order> orders
    ) {
        if (service.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        partyOrderRelationService.replaceOrders(
                id, orders, PartyOrderRelationService.SUPPLIER_PICKUP_PARTY_TYPE);
        return ResponseEntity.ok(partyOrderRelationService.findActiveOrders(
                id, PartyOrderRelationService.SUPPLIER_PICKUP_PARTY_TYPE));
    }
}
