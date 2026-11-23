package com.example.demo.service;

import com.example.demo.dto.DeliveryRetrieveResponse;
import com.example.demo.dto.SupplierPickupRetrieveResponse;
import com.example.demo.dto.TripDeliveryDetailsResponse;
import com.example.demo.dto.TripRetrieveResponse;
import com.example.demo.entity.Expenses;
import com.example.demo.entity.Mortality;
import com.example.demo.entity.SupplierPickup;
import com.example.demo.entity.ActiveRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TripDeliveryDetailsService {
    private final TripService tripService;
    private final DeliveryService deliveryService;
    private final SupplierPickupService supplierPickupService;
    private final MortalityService mortalityService;
    private final ExpensesService expensesService;

    public TripDeliveryDetailsService(
            TripService tripService,
            DeliveryService deliveryService,
            SupplierPickupService supplierPickupService,
            MortalityService mortalityService,
            ExpensesService expensesService
    ) {
        this.tripService = tripService;
        this.deliveryService = deliveryService;
        this.supplierPickupService = supplierPickupService;
        this.mortalityService = mortalityService;
        this.expensesService = expensesService;
    }

    @Transactional(readOnly = true)
    public Optional<TripDeliveryDetailsResponse> retrieve(Integer tripId) {
        if (tripId == null || tripId <= 0) {
            throw new IllegalArgumentException("Trip ID must be a positive integer.");
        }

        Optional<TripRetrieveResponse> trip = tripService.retrieve(tripId);
        if (trip.isEmpty()) {
            return Optional.empty();
        }

        List<DeliveryRetrieveResponse> deliveries = deliveryService.retrieveByTripId(tripId);

        // Actual supplier pickup records are linked from the trip's delivery records.
        // Preserve insertion order and avoid returning the same pickup more than once.
        Map<Integer, SupplierPickupRetrieveResponse> pickupsById = new LinkedHashMap<>();
        for (DeliveryRetrieveResponse delivery : deliveries) {
            SupplierPickup linkedPickup = delivery.getSupplierPickup();
            if (linkedPickup == null || linkedPickup.getId() == null
                    || !ActiveRecord.isActiveRecord(linkedPickup)) {
                continue;
            }

            supplierPickupService.retrieve(linkedPickup.getId()).ifPresent(pickup -> {
                if (Boolean.TRUE.equals(pickup.getIsActive())
                        && !Boolean.TRUE.equals(pickup.getIsDeleted())) {
                    pickupsById.putIfAbsent(pickup.getId(), pickup);
                }
            });
        }

        List<Mortality> mortalities = mortalityService.findByTripId(tripId);
        List<Expenses> expenses = expensesService.findByTripId(tripId);

        return Optional.of(TripDeliveryDetailsResponse.builder()
                .trip(trip.get())
                .supplierPickups(List.copyOf(pickupsById.values()))
                .deliveries(deliveries)
                .mortalities(mortalities)
                .expenses(expenses)
                .build());
    }
}
