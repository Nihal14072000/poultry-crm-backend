package com.example.demo.service;

import com.example.demo.entity.Payment;
import com.example.demo.entity.Trip;
import com.example.demo.repository.BuyingPartyRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.FinancePaymentAllocationRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PartyOrderRelationRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.SellingPartyRepository;
import com.example.demo.repository.TripRepository;
import com.example.demo.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {
    private final PaymentRepository repository = mock(PaymentRepository.class);
    private final PartyOrderRelationRepository partyOrderRelationRepository =
            mock(PartyOrderRelationRepository.class);
    private final BuyingPartyRepository buyingPartyRepository = mock(BuyingPartyRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final TripRepository tripRepository = mock(TripRepository.class);
    private final SellingPartyRepository sellingPartyRepository = mock(SellingPartyRepository.class);
    private final FinancePaymentAllocationRepository allocationRepository =
            mock(FinancePaymentAllocationRepository.class);
    private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    private final TripPartyRelationService tripPartyRelationService =
            mock(TripPartyRelationService.class);
    private final PartyOrderRelationService partyOrderRelationService =
            mock(PartyOrderRelationService.class);

    private final PaymentService service = new PaymentService(
            repository,
            partyOrderRelationRepository,
            buyingPartyRepository,
            orderRepository,
            tripRepository,
            sellingPartyRepository,
            allocationRepository,
            vehicleRepository,
            employeeRepository,
            tripPartyRelationService,
            partyOrderRelationService
    );

    @Test
    void buyingPartyIdSetsSellerPayoutType() {
        var payment = payment();
        payment.setBuyingPartyId(1);
        payment.setPaymentType("SELLER_PAYOUT");

        service.create(payment);

        assertEquals("SELLER_PAYOUT", payment.getPaymentType());
    }

    @Test
    void sellingPartyIdSetsBuyerReceiptType() {
        var payment = payment();
        payment.setSellingPartyId(1);
        payment.setPaymentType("BUYER_RECEIPT");

        service.create(payment);

        assertEquals("BUYER_RECEIPT", payment.getPaymentType());
    }

    @Test
    void rejectsPaymentWithBothPartyIds() {
        var payment = payment();
        payment.setBuyingPartyId(1);
        payment.setSellingPartyId(2);

        assertThrows(ResponseStatusException.class, () -> service.create(payment));
    }

    @Test
    void retrieveByTripIdReturnsEmptyWhenTripHasNoPayments() {
        when(repository.findActiveByTripIdOrderByCreateDateDesc(42)).thenReturn(List.of());

        assertEquals(List.of(), service.retrieveByTripId(42));
    }

    @Test
    void retrieveByTripIdLoadsTripOnceAndReusesItsResponseForEveryPayment() {
        Payment first = payment();
        first.setId(1);
        first.setTripId(42);
        Payment second = payment();
        second.setId(2);
        second.setTripId(42);

        when(repository.findActiveByTripIdOrderByCreateDateDesc(42))
                .thenReturn(List.of(first, second));
        when(tripRepository.findById(42)).thenReturn(Optional.of(new Trip()));
        when(tripPartyRelationService.findActiveByTripId(42)).thenReturn(List.of());
        when(allocationRepository.findByPaymentIdIn(Set.of(1, 2))).thenReturn(List.of());

        var responses = service.retrieveByTripId(42);

        assertEquals(2, responses.size());
        assertNotNull(responses.get(0).getTrip());
        assertSame(responses.get(0).getTrip(), responses.get(1).getTrip());
        verify(tripRepository).findById(42);
        verify(allocationRepository).findByPaymentIdIn(Set.of(1, 2));
    }

    private Payment payment() {
        var payment = new Payment();
        payment.setAmount(BigDecimal.ONE);
        payment.setIsActive(true);
        payment.setIsDeleted(false);
        return payment;
    }
}
