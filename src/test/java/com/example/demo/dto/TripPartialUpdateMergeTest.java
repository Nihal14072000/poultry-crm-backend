package com.example.demo.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TripPartialUpdateMergeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void omittedPartyListsRemainUnchangedAndEmptyListsClearOnlyThatPartyType() throws Exception {
        TripRetrieveResponse current = TripRetrieveResponse.builder()
                .route("Old route")
                .buyingParties(List.of(BuyingPartydto.builder().id(10).name("Buyer").build()))
                .sellingParties(List.of(SellingPartydto.builder().id(20).name("Seller").build()))
                .build();

        JsonNode routeOnlyUpdate = objectMapper.readTree("{\"route\":\"New route\"}");
        TripRetrieveResponse merged = objectMapper.readerForUpdating(current)
                .readValue(routeOnlyUpdate.traverse(objectMapper));

        assertEquals("New route", merged.getRoute());
        assertEquals(10, merged.getBuyingParties().get(0).getId());
        assertEquals(20, merged.getSellingParties().get(0).getId());

        JsonNode clearBuyers = objectMapper.readTree("{\"buyingParties\":[]}");
        TripRetrieveResponse afterClear = objectMapper.readerForUpdating(merged)
                .readValue(clearBuyers.traverse(objectMapper));

        assertEquals(List.of(), afterClear.getBuyingParties());
        assertEquals(20, afterClear.getSellingParties().get(0).getId());
    }
}
