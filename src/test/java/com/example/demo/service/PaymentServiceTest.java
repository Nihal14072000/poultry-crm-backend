package com.example.demo.service;

import com.example.demo.entity.Payment;
import com.example.demo.repository.FinancePaymentAllocationRepository;
import com.example.demo.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

class PaymentServiceTest {
    private final PaymentRepository repository = mock(PaymentRepository.class);
    private final PaymentService service = new PaymentService(
            repository, null, null, null, null, null, null, null, null, null);

    @Test
    void buyingPartyIdSetsBuyerReceiptType() {
        var payment = payment();
        payment.setBuyingPartyId(1);
        payment.setPaymentType("SELLER_PAYOUT");

        service.create(payment);

        assertEquals("BUYER_RECEIPT", payment.getPaymentType());
    }

    @Test
    void sellingPartyIdSetsSellerPayoutType() {
        var payment = payment();
        payment.setSellingPartyId(1);
        payment.setPaymentType("BUYER_RECEIPT");

        service.create(payment);

        assertEquals("SELLER_PAYOUT", payment.getPaymentType());
    }

    @Test
    void rejectsPaymentWithBothPartyIds() {
        var payment = payment();
        payment.setBuyingPartyId(1);
        payment.setSellingPartyId(2);

        assertThrows(ResponseStatusException.class, () -> service.create(payment));
    }

    @Test
    void deletesAllocationsBeforePaymentsForTrip() {
        var paymentRepository = mock(PaymentRepository.class);
        var allocationRepository = mock(FinancePaymentAllocationRepository.class);
        var service = new PaymentService(
                paymentRepository, null, null, null, null, allocationRepository, null, null, null, null);

        service.deleteByTripId(7);

        InOrder deletionOrder = inOrder(allocationRepository, paymentRepository);
        deletionOrder.verify(allocationRepository).deleteByPaymentTripId(7);
        deletionOrder.verify(paymentRepository).deleteByTripId(7);
    }

    private Payment payment() {
        var payment = new Payment();
        payment.setAmount(BigDecimal.ONE);
        return payment;
    }
}
