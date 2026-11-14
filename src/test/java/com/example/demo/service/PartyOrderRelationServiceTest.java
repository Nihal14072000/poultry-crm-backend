package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.repository.PartyOrderRelationRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PartyOrderRelationServiceTest {
    @Test
    void addOrdersAssignsTheTripIdToNewOrders() {
        var repository = mock(PartyOrderRelationRepository.class);
        var orderService = mock(OrderService.class);
        var order = new Order();
        when(repository.findByPartyIdAndPartyType(2, "B")).thenReturn(List.of());
        when(orderService.create(order)).thenReturn(order);

        var service = new PartyOrderRelationService(repository, orderService);
        service.addOrders(2, List.of(order), "B", 7);

        assertEquals(7, order.getTripId());
        verify(orderService).create(order);
        verify(repository).saveAll(anyList());
    }
}
