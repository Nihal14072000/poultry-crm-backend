package com.example.demo.controller;

import com.example.demo.entity.DeliveryPartyRelation;
import com.example.demo.service.DeliveryPartyRelationService;
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
@RequestMapping("/api/delivery-party-relations")
public class DeliveryPartyRelationController {
    private final DeliveryPartyRelationService service;

    public DeliveryPartyRelationController(DeliveryPartyRelationService service) {
        this.service = service;
    }

    @GetMapping
    public List<DeliveryPartyRelation> findAll() {
        return service.findAll();
    }

    @GetMapping("/delivery/{deliveryId}")
    public List<DeliveryPartyRelation> findByDeliveryId(@PathVariable Integer deliveryId) {
        return service.findByDeliveryId(deliveryId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryPartyRelation> findById(@PathVariable Integer id) {
        return service.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DeliveryPartyRelation> create(@RequestBody DeliveryPartyRelation relation) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(relation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliveryPartyRelation> update(
            @PathVariable Integer id,
            @RequestBody DeliveryPartyRelation relation
    ) {
        return service.update(id, relation).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
