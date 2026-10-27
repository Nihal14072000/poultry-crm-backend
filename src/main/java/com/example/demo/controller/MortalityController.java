package com.example.demo.controller;

import com.example.demo.entity.Mortality;
import com.example.demo.service.MortalityService;
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
@RequestMapping("/api/mortalities")
public class MortalityController {
    private final MortalityService service;

    public MortalityController(MortalityService service) {
        this.service = service;
    }

    @GetMapping
    public List<Mortality> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mortality> findById(@PathVariable Integer id) {
        return service.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Mortality> create(@RequestBody Mortality mortality) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(mortality));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mortality> update(@PathVariable Integer id, @RequestBody Mortality mortality) {
        return service.update(id, mortality).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
