package com.example.demo.controller;

import com.example.demo.entity.Payment;
import com.example.demo.service.PaymentService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentControllerTest {
    private final PaymentService service = mock(PaymentService.class);
    private final PaymentController controller = new PaymentController(service);

    @Test
    void patchChangesSuppliedFieldsAndPreservesOmittedFields() throws Exception {
        Payment current = new Payment();
        current.setId(7);
        current.setBuyingPartyId(12);
        current.setAmount(new BigDecimal("125.00"));
        current.setNote("Original note");
        current.setPaymentType("SELLER_PAYOUT");
        current.setPaymentMethod("CASH");
        current.setExternalReference("reference-1");
        current.setStatus("PENDING");
        when(service.findById(7)).thenReturn(Optional.of(current));
        when(service.update(eq(7), any(Payment.class)))
                .thenAnswer(invocation -> Optional.of(invocation.getArgument(1)));

        Map<String, Object> patch = new HashMap<>();
        patch.put("amount", 150.00);
        patch.put("note", "Updated note");
        patch.put("externalReference", null);
        Payment patched = null;

        assertEquals(new BigDecimal("150.00"), patched.getAmount());
        assertEquals("Updated note", patched.getNote());
        assertNull(patched.getExternalReference());
        assertEquals(12, patched.getBuyingPartyId());
        assertEquals("SELLER_PAYOUT", patched.getPaymentType());
        assertEquals("CASH", patched.getPaymentMethod());
        assertEquals("PENDING", patched.getStatus());
    }

}
