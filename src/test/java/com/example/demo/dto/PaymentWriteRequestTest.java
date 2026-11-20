package com.example.demo.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentWriteRequestTest {

    @Test
    void acceptsExistingPaymentPayloadAndIgnoresServerManagedProperties() throws Exception {
        String json = """
                {
                  "buyingPartyId": 12,
                  "tripId": 31,
                  "amount": 2500.00,
                  "status": "COMPLETED",
                  "isDeleted": true,
                  "isActive": false,
                  "createdBy": 999,
                  "updatedBy": 999,
                  "createDate": "2026-11-21T10:00:00"
                }
                """;

        PaymentWriteRequest request = new ObjectMapper().readValue(json, PaymentWriteRequest.class);

        assertEquals(12, request.getBuyingPartyId());
        assertEquals(31, request.getTripId());
        assertEquals("COMPLETED", request.getStatus());
    }
}
