package com.example.demo.controller;

import com.example.demo.entity.BuyingParty;
import com.example.demo.service.BuyingPartyService;
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
@RequestMapping("/api/buying-parties")
public class BuyingPartyController {
    private final BuyingPartyService service;

    public BuyingPartyController(BuyingPartyService service) {
        this.service = service;
    }

    @GetMapping
    public List<BuyingParty> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BuyingParty> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BuyingParty> create(@RequestBody BuyingParty buyingParty) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(buyingParty));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BuyingParty> update(
            @PathVariable Integer id,
            @RequestBody BuyingParty buyingParty
    ) {
        return service.update(id, buyingParty)
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
