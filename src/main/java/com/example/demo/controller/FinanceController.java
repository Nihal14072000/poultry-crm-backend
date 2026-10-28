package com.example.demo.controller;

import com.example.demo.entity.FinanceOrderSettlement;
import com.example.demo.entity.FinancePaymentAllocation;
import com.example.demo.service.FinanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {
    private final FinanceService service;
    public FinanceController(FinanceService service) { this.service = service; }

    @GetMapping("/settlements")
    public List<FinanceOrderSettlement> settlements() { return service.settlements(); }

    @GetMapping("/trips/{tripId}/settlements")
    public List<FinanceOrderSettlement> tripSettlements(@PathVariable Integer tripId) { return service.tripSettlements(tripId); }

    @PostMapping("/settlements")
    public ResponseEntity<FinanceOrderSettlement> createSettlement(@RequestBody FinanceOrderSettlement settlement) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createSettlement(settlement));
    }

    @PostMapping("/allocations")
    public ResponseEntity<FinancePaymentAllocation> allocate(@RequestBody FinancePaymentAllocation allocation) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.allocate(allocation));
    }

    @GetMapping("/receivables")
    public List<Map<String, Object>> receivables(@RequestParam(required = false) Integer tripId,
                                                 @RequestParam(required = false) Integer buyingPartyId) {
        return service.buyers(tripId, buyingPartyId);
    }

    @GetMapping("/payables")
    public List<Map<String, Object>> payables(@RequestParam(required = false) Integer tripId,
                                              @RequestParam(required = false) Integer sellingPartyId) {
        return service.sellers(tripId, sellingPartyId);
    }

    @GetMapping("/trips/summary")
    public List<Map<String, Object>> tripSummary(@RequestParam(required = false) Integer tripId) {
        return service.tripSummaries(tripId);
    }
}
