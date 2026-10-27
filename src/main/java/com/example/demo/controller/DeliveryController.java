package com.example.demo.controller;

import com.example.demo.entity.Delivery;
import com.example.demo.dto.DeliveryRetrieveResponse;
import com.example.demo.service.DeliveryService;
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
@RequestMapping("/api/deliveries")
public class DeliveryController {
    private final DeliveryService service;

    public DeliveryController(DeliveryService service) {
        this.service = service;
    }

    @GetMapping
    public List<Delivery> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Delivery> findById(@PathVariable Integer id) {
        return service.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/retrieve")
    @Operation(
            summary = "Retrieve delivery details",
            description = "Returns the delivery with its related trip and vendor deliveries. Returns 404 if the delivery does not exist."
    )
    public ResponseEntity<DeliveryRetrieveResponse> retrieve(@PathVariable Integer id) {
        return service.retrieve(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Delivery> create(@RequestBody Delivery delivery) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(delivery));
    }

    @PostMapping("/insert")
    @Operation(
            summary = "Insert a delivery with vendor deliveries",
            description = "Creates a delivery and its vendor deliveries, selling parties, and orders."
    )
    public ResponseEntity<DeliveryRetrieveResponse> insert(
            @RequestBody DeliveryRetrieveResponse delivery
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createDelivery(delivery));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Delivery> update(@PathVariable Integer id, @RequestBody Delivery delivery) {
        return service.update(id, delivery).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
