package com.example.demo.service;

import com.example.demo.entity.FinanceOrderSettlement;
import com.example.demo.entity.FinancePaymentAllocation;
import com.example.demo.entity.Payment;
import com.example.demo.entity.Expenses;
import com.example.demo.entity.Trip;
import com.example.demo.dto.TripRetrieveResponse;
import com.example.demo.repository.FinanceOrderSettlementRepository;
import com.example.demo.repository.FinancePaymentAllocationRepository;
import com.example.demo.repository.FinanceReceivablesRepository;
import com.example.demo.repository.PaymentRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class FinanceServiceTest {
    @Test
    void allocationUsesThePaymentType() {
        var settlements = mock(FinanceOrderSettlementRepository.class);
        var allocations = mock(FinancePaymentAllocationRepository.class);
        var payments = mock(PaymentRepository.class);
        var payment = new Payment();
        payment.setId(1); payment.setPaymentType("BUYER_RECEIPT"); payment.setBuyingPartyId(2);
        payment.setAmount(BigDecimal.TEN); payment.setStatus("COMPLETED");
        var settlement = new FinanceOrderSettlement();
        settlement.setId(3); settlement.setBuyingPartyId(2); settlement.setBuyerDueAmount(BigDecimal.TEN);
        when(payments.findById(1)).thenReturn(Optional.of(payment));
        when(settlements.findById(3)).thenReturn(Optional.of(settlement));
        when(allocations.sumAllocatedForPayment(1)).thenReturn(BigDecimal.ZERO);
        when(allocations.sumCompletedForSettlement(3, "BUYER_RECEIPT")).thenReturn(BigDecimal.ZERO);
        when(allocations.save(any())).thenAnswer(call -> call.getArgument(0));

        var allocation = new FinancePaymentAllocation();
        allocation.setPaymentId(1); allocation.setSettlementId(3); allocation.setAllocatedAmount(BigDecimal.TEN);
        new FinanceService(settlements, allocations, payments, mock(FinanceReceivablesRepository.class)).allocate(allocation);

        assertEquals("BUYER_RECEIPT", allocation.getAllocationType());
    }

    @Test
    void tripCreationSavesEachInitialPaymentAndExpense() {
        var tripRepository = mock(com.example.demo.repository.TripRepository.class);
        var relations = mock(TripPartyRelationService.class);
        var paymentService = mock(PaymentService.class);
        var expensesService = mock(ExpensesService.class);
        var savedTrip = new Trip(); savedTrip.setTripId(7);
        when(tripRepository.save(any())).thenReturn(savedTrip);
        when(relations.findActiveByTripId(7)).thenReturn(java.util.List.of());
        var payment = new Payment(); payment.setPaymentType("BUYER_RECEIPT"); payment.setBuyingPartyId(2); payment.setAmount(BigDecimal.ONE); payment.setStatus("COMPLETED");
        var expense = new Expenses(); expense.setAmount(BigDecimal.ONE); expense.setExpenseDate(java.time.LocalDate.now()); expense.setCategory("Fuel");
        var request = new TripRetrieveResponse(); request.setPayments(java.util.List.of(payment)); request.setExpenses(java.util.List.of(expense));
        var service = new TripService(tripRepository, mock(VehicleService.class), mock(DriverService.class), mock(SupervisorService.class), mock(BuyingPartyService.class), mock(SellingPartyService.class), relations, mock(PartyOrderRelationService.class), paymentService, expensesService);

        service.createTrip(request);

        assertEquals(7, payment.getTripId()); assertEquals(7, expense.getTripId());
        verify(paymentService).create(payment); verify(expensesService).create(expense);
    }
}
