package com.example.demo.controller;

import com.example.demo.entity.VendorDelivery;
import com.example.demo.entity.Order;
import com.example.demo.service.PartyOrderRelationService;
import com.example.demo.service.VendorDeliveryService;
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
@RequestMapping("/api/vendor-deliveries")
public class VendorDeliveryController {
    private final VendorDeliveryService service;
    private final PartyOrderRelationService partyOrderRelationService;

    public VendorDeliveryController(
            VendorDeliveryService service,
            PartyOrderRelationService partyOrderRelationService
    ) {
        this.service = service;
        this.partyOrderRelationService = partyOrderRelationService;
    }

    @GetMapping
    public List<VendorDelivery> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorDelivery> findById(@PathVariable Integer id) {
        return service.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<VendorDelivery> create(@RequestBody VendorDelivery delivery) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(delivery));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendorDelivery> update(@PathVariable Integer id, @RequestBody VendorDelivery delivery) {
        return service.update(id, delivery).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
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
                id, PartyOrderRelationService.VENDOR_DELIVERY_PARTY_TYPE));
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
                id, orders, PartyOrderRelationService.VENDOR_DELIVERY_PARTY_TYPE);
        return ResponseEntity.ok(partyOrderRelationService.findActiveOrders(
                id, PartyOrderRelationService.VENDOR_DELIVERY_PARTY_TYPE));
    }
}
