package com.example.demo.controller;

import com.example.demo.dto.FinanceSummaryResponse;
import com.example.demo.dto.FinanceSettlementWriteRequest;
import com.example.demo.dto.FinanceAllocationWriteRequest;
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
    public ResponseEntity<FinanceOrderSettlement> createSettlement(@RequestBody FinanceSettlementWriteRequest request) {
        FinanceOrderSettlement settlement = new FinanceOrderSettlement();
        settlement.setTripId(request.getTripId());
        settlement.setOrderId(request.getOrderId());
        settlement.setBuyingPartyId(request.getBuyingPartyId());
        settlement.setSellingPartyId(request.getSellingPartyId());
        settlement.setSettlementDate(request.getSettlementDate());
        settlement.setDueDate(request.getDueDate());
        settlement.setCurrencyCode(request.getCurrencyCode());
        settlement.setOrderAmount(request.getOrderAmount());
        settlement.setBrokerageAmount(request.getBrokerageAmount());
        settlement.setBrokeragePayer(request.getBrokeragePayer());
        settlement.setNotes(request.getNotes());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createSettlement(settlement));
    }

    @PostMapping("/allocations")
    public ResponseEntity<FinancePaymentAllocation> allocate(@RequestBody FinanceAllocationWriteRequest request) {
        FinancePaymentAllocation allocation = new FinancePaymentAllocation();
        allocation.setPaymentId(request.getPaymentId());
        allocation.setSettlementId(request.getSettlementId());
        allocation.setAllocatedAmount(request.getAllocatedAmount());
        allocation.setAllocationDate(request.getAllocationDate());
        allocation.setNote(request.getNote());
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

    @GetMapping("/summary")
    public FinanceSummaryResponse summary(
            @RequestParam(required = false) Integer buyingPartyId,
            @RequestParam(required = false) Integer sellingPartyId) {
        return service.summary(buyingPartyId, sellingPartyId);
    }
}
