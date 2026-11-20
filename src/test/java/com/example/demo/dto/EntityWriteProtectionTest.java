package com.example.demo.dto;

import com.example.demo.entity.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityWriteProtectionTest {

    @Test
    void ignoresServerManagedPaymentFieldsDuringEntityDeserialization() throws Exception {
        String json = """
                {
                  "amount": 125.00,
                  "isActive": false,
                  "isDeleted": true,
                  "createdBy": 999,
                  "updatedBy": 999,
                  "createDate": "2026-11-21T10:00:00",
                  "updatedDate": "2026-11-21T11:00:00"
                }
                """;

        Payment payment = new ObjectMapper().readValue(json, Payment.class);

        assertTrue(payment.getIsActive());
        assertEquals(false, payment.getIsDeleted());
        assertNull(payment.getCreatedBy());
        assertNull(payment.getUpdatedBy());
        assertNull(payment.getCreateDate());
        assertNull(payment.getUpdatedDate());
    }
}
