package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.PartyOrderRelation;
import com.example.demo.repository.PartyOrderRelationRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PartyOrderRelationServiceTest {
    @Test
    void addOrdersAssignsTheTripIdToNewOrders() {
        var repository = mock(PartyOrderRelationRepository.class);
        var orderService = mock(OrderService.class);
        var order = new Order();
        when(repository.findByPartyIdAndPartyTypeAndTripId(2, "B", 7)).thenReturn(List.of());
        when(orderService.create(order)).thenReturn(order);

        var service = new PartyOrderRelationService(repository, orderService);
        service.addOrders(2, List.of(order), "B", 7);

        assertEquals(7, order.getTripId());
        verify(orderService).create(order);
        verify(repository).findByPartyIdAndPartyTypeAndTripId(2, "B", 7);
        verify(repository, never()).findByPartyIdAndPartyType(2, "B");
        verify(repository).saveAll(anyList());
    }

    @Test
    void replaceOrdersDeactivatesOnlyRelationsLoadedForTheSpecifiedTrip() {
        var repository = mock(PartyOrderRelationRepository.class);
        var orderService = mock(OrderService.class);
        var existingTripRelation = new PartyOrderRelation();
        existingTripRelation.setIsActive(true);
        existingTripRelation.setIsDeleted(false);
        var replacementOrder = new Order();
        replacementOrder.setId(99);
        replacementOrder.setTripId(7);

        when(repository.findByPartyIdAndPartyTypeAndTripId(2, "B", 7))
                .thenReturn(List.of(existingTripRelation));
        when(orderService.findById(99)).thenReturn(Optional.of(replacementOrder));
        when(orderService.update(99, replacementOrder)).thenReturn(Optional.of(replacementOrder));

        var service = new PartyOrderRelationService(repository, orderService);
        service.replaceOrders(2, List.of(replacementOrder), "B", 7);

        assertEquals(false, existingTripRelation.getIsActive());
        assertEquals(true, existingTripRelation.getIsDeleted());
        verify(repository).findByPartyIdAndPartyTypeAndTripId(2, "B", 7);
        verify(repository, never()).findByPartyIdAndPartyType(2, "B");
        verify(repository).saveAll(anyList());
    }

    @Test
    void replacingSupplierPickupOrdersDeactivatesOnlyThatPickupRelations() {
        assertActualOrderRelationsAreScopedToRecord(
                25, PartyOrderRelationService.SUPPLIER_PICKUP_PARTY_TYPE);
    }

    @Test
    void replacingVendorDeliveryOrdersDeactivatesOnlyThatDeliveryRelations() {
        assertActualOrderRelationsAreScopedToRecord(
                35, PartyOrderRelationService.VENDOR_DELIVERY_PARTY_TYPE);
    }

    private void assertActualOrderRelationsAreScopedToRecord(Integer recordId, String partyType) {
        var repository = mock(PartyOrderRelationRepository.class);
        var orderService = mock(OrderService.class);
        var actualRecordRelation = new PartyOrderRelation();
        actualRecordRelation.setIsActive(true);
        actualRecordRelation.setIsDeleted(false);
        when(repository.findByPartyIdAndPartyType(recordId, partyType))
                .thenReturn(List.of(actualRecordRelation));

        var service = new PartyOrderRelationService(repository, orderService);
        service.replaceOrders(recordId, List.of(), partyType);

        assertEquals(false, actualRecordRelation.getIsActive());
        assertEquals(true, actualRecordRelation.getIsDeleted());
        verify(repository).findByPartyIdAndPartyType(recordId, partyType);
        verify(repository, never()).findByPartyIdAndPartyTypeAndTripId(
                recordId, partyType, null);
        verify(repository).saveAll(anyList());
    }
}
