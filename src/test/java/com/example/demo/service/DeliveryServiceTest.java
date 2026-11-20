package com.example.demo.service;

import com.example.demo.repository.DeliveryRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeliveryServiceTest {
    private final DeliveryRepository repository = mock(DeliveryRepository.class);
    private final DeliveryService service = new DeliveryService(
            repository, null, null, null, null, null, null, null, null, null);

    @Test
    void retrieveByTripIdReturnsEmptyWhenTripHasNoDeliveries() {
        when(repository.findByTripId(42)).thenReturn(List.of());

        assertEquals(List.of(), service.retrieveByTripId(42));
    }
}
