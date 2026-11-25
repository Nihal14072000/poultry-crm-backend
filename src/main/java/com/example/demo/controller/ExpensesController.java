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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpensesController {
    private final ExpensesService service;

    public ExpensesController(ExpensesService service) {
        this.service = service;
    }

    public record PagedExpensesResponse(
            List<Expenses> items,
            int page,
            int size,
            long totalElements,
            int totalPages,
            BigDecimal totalAmount
    ) {}

    @GetMapping
    public List<Expenses> findAll() {
        return service.findAll();
    }

    @GetMapping("/paged")
    public PagedExpensesResponse findPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer tripId,
            @RequestParam(required = false) String query
    ) {
        var result = service.findPage(page, size, tripId, query);
        return new PagedExpensesResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                service.sumAmount(tripId, query)
        );
    }

    /**
     * Returns only active, non-deleted expenses belonging to the requested trip.
     * Filtering is performed by the database, not by the client.
     */
    @GetMapping("/trip/{tripId}")
    public List<Expenses> findByTripId(@PathVariable Integer tripId) {
        return service.findByTripId(tripId);
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
