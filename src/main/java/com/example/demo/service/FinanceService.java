package com.example.demo.service;

import com.example.demo.entity.FinanceOrderSettlement;
import com.example.demo.entity.FinancePaymentAllocation;
import com.example.demo.entity.Payment;
import com.example.demo.repository.FinanceOrderSettlementRepository;
import com.example.demo.repository.FinancePaymentAllocationRepository;
import com.example.demo.repository.FinanceReceivablesRepository;
import com.example.demo.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class FinanceService {
    private final FinanceOrderSettlementRepository settlements;
    private final FinancePaymentAllocationRepository allocations;
    private final PaymentRepository payments;
    private final FinanceReceivablesRepository views;

    public FinanceService(FinanceOrderSettlementRepository settlements, FinancePaymentAllocationRepository allocations,
                         PaymentRepository payments, FinanceReceivablesRepository views) {
        this.settlements = settlements; this.allocations = allocations; this.payments = payments; this.views = views;
    }

    public List<FinanceOrderSettlement> settlements() { return settlements.findAll(); }
    public List<FinanceOrderSettlement> tripSettlements(Integer tripId) { return settlements.findByTripIdAndStatusNot(tripId, "VOID"); }

    @Transactional
    public FinanceOrderSettlement createSettlement(FinanceOrderSettlement settlement) {
        if (settlement.getSettlementDate() == null) settlement.setSettlementDate(LocalDate.now());
        if (settlement.getBrokerageAmount() == null) settlement.setBrokerageAmount(BigDecimal.ZERO);
        if (settlement.getCurrencyCode() == null) settlement.setCurrencyCode("INR");
        if (settlement.getStatus() == null) settlement.setStatus("OPEN");
        return settlements.save(settlement);
    }

    @Transactional
    public FinancePaymentAllocation allocate(FinancePaymentAllocation allocation) {
        Payment payment = payments.findById(allocation.getPaymentId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        FinanceOrderSettlement settlement = settlements.findById(allocation.getSettlementId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Settlement not found"));
        String type = payment.getPaymentType();
        if (!List.of("BUYER_RECEIPT", "SELLER_PAYOUT").contains(type))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment type is not allocatable");
        allocation.setAllocationType(type);
        boolean partyMatches = "BUYER_RECEIPT".equals(type)
                ? payment.getBuyingPartyId() != null && payment.getBuyingPartyId().equals(settlement.getBuyingPartyId())
                : payment.getSellingPartyId() != null && payment.getSellingPartyId().equals(settlement.getSellingPartyId());
        if (!partyMatches) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment party does not match settlement");
        if (payment.getStatus() == null || !"COMPLETED".equals(payment.getStatus()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only completed payments can be allocated");
        BigDecimal paymentRemaining = payment.getAmount().subtract(allocations.sumAllocatedForPayment(payment.getId()));
        BigDecimal due = "BUYER_RECEIPT".equals(type) ? settlement.getBuyerDueAmount() : settlement.getSellerDueAmount();
        BigDecimal settlementRemaining = due.subtract(allocations.sumCompletedForSettlement(settlement.getId(), type));
        if (allocation.getAllocatedAmount() == null || allocation.getAllocatedAmount().signum() <= 0
                || allocation.getAllocatedAmount().compareTo(paymentRemaining) > 0
                || allocation.getAllocatedAmount().compareTo(settlementRemaining) > 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allocation exceeds payment or outstanding balance");
        if (allocation.getAllocationDate() == null) allocation.setAllocationDate(LocalDate.now());
        return allocations.save(allocation);
    }

    public List<Map<String, Object>> buyers(Integer tripId, Integer partyId) { return views.buyers(tripId, partyId); }
    public List<Map<String, Object>> sellers(Integer tripId, Integer partyId) { return views.sellers(tripId, partyId); }
    public List<Map<String, Object>> tripSummaries(Integer tripId) { return views.trips(tripId); }
}
