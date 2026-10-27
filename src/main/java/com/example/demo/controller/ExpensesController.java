package com.example.demo.controller;

import com.example.demo.entity.Expenses;
import com.example.demo.service.ExpensesService;
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
@RequestMapping("/api/expenses")
public class ExpensesController {
    private final ExpensesService service;

    public ExpensesController(ExpensesService service) {
        this.service = service;
    }

    @GetMapping
    public List<Expenses> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expenses> findById(@PathVariable Integer id) {
        return service.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Expenses> create(@RequestBody Expenses expenses) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(expenses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expenses> update(@PathVariable Integer id, @RequestBody Expenses expenses) {
        return service.update(id, expenses).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
