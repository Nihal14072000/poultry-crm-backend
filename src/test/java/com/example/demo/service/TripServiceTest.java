package com.example.demo.service;

import com.example.demo.repository.TripRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripServiceTest {
    @Test
    void deleteRemovesTripOrdersAndPaymentsBeforeTrip() {
        TripRepository tripRepository = mock(TripRepository.class);
        PartyOrderRelationService partyOrderRelationService = mock(PartyOrderRelationService.class);
        PaymentService paymentService = mock(PaymentService.class);
        ExpensesService expensesService = mock(ExpensesService.class);
        TripService service = createService(
                tripRepository, partyOrderRelationService, paymentService, expensesService);
        when(tripRepository.existsById(7)).thenReturn(true);

        assertTrue(service.delete(7));

        InOrder deletionOrder = inOrder(partyOrderRelationService, paymentService, expensesService, tripRepository);
        deletionOrder.verify(partyOrderRelationService).deleteOrdersByTripId(7);
        deletionOrder.verify(paymentService).deleteByTripId(7);
        deletionOrder.verify(expensesService).deleteByTripId(7);
        deletionOrder.verify(tripRepository).deleteById(7);
    }

    @Test
    void deleteDoesNotRemoveRelatedRecordsWhenTripDoesNotExist() {
        TripRepository tripRepository = mock(TripRepository.class);
        PartyOrderRelationService partyOrderRelationService = mock(PartyOrderRelationService.class);
        PaymentService paymentService = mock(PaymentService.class);
        ExpensesService expensesService = mock(ExpensesService.class);
        TripService service = createService(
                tripRepository, partyOrderRelationService, paymentService, expensesService);
        when(tripRepository.existsById(7)).thenReturn(false);

        assertFalse(service.delete(7));

        verify(partyOrderRelationService, never()).deleteOrdersByTripId(7);
        verify(paymentService, never()).deleteByTripId(7);
        verify(expensesService, never()).deleteByTripId(7);
        verify(tripRepository, never()).deleteById(7);
    }

    private TripService createService(
            TripRepository tripRepository,
            PartyOrderRelationService partyOrderRelationService,
            PaymentService paymentService,
            ExpensesService expensesService
    ) {
        return new TripService(
                tripRepository,
                mock(VehicleService.class),
                mock(DriverService.class),
                mock(SupervisorService.class),
                mock(EmployeeService.class),
                mock(BuyingPartyService.class),
                mock(SellingPartyService.class),
                mock(TripPartyRelationService.class),
                partyOrderRelationService,
                paymentService,
                expensesService);
    }
}
