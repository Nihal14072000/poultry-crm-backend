package com.example.demo.controller;

import com.example.demo.entity.SellingParty;
import com.example.demo.service.SellingPartyService;
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
@RequestMapping("/api/selling-parties")
public class SellingPartyController {
    private final SellingPartyService service;

    public SellingPartyController(SellingPartyService service) {
        this.service = service;
    }

    @GetMapping
    public List<SellingParty> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SellingParty> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SellingParty> create(@RequestBody SellingParty sellingParty) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(sellingParty));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SellingParty> update(
            @PathVariable Integer id,
            @RequestBody SellingParty sellingParty
    ) {
        return service.update(id, sellingParty)
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
