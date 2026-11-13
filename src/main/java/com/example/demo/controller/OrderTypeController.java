package com.example.demo.controller;

import com.example.demo.entity.OrderType;
import com.example.demo.service.OrderTypeService;
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
@RequestMapping("/api/order-types")
public class OrderTypeController {
    private final OrderTypeService service;

    public OrderTypeController(OrderTypeService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrderType> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderType> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderType> create(@RequestBody OrderType orderType) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(orderType));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderType> update(
            @PathVariable Integer id,
            @RequestBody OrderType orderType
    ) {
        return service.update(id, orderType)
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
