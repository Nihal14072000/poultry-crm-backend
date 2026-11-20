package com.example.demo.service;

import com.example.demo.repository.MortalityRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MortalityServiceTest {
    private final MortalityRepository repository = mock(MortalityRepository.class);
    private final MortalityService service = new MortalityService(repository);

    @Test
    void findByTripIdReturnsEmptyWhenTripHasNoMortalityRecords() {
        when(repository.findByTripId(42)).thenReturn(List.of());

        assertEquals(List.of(), service.findByTripId(42));
    }
}
