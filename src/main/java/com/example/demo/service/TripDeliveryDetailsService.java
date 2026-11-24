package com.example.demo.service;

import com.example.demo.dto.DeliveryRetrieveResponse;
import com.example.demo.dto.SupplierPickupRetrieveResponse;
import com.example.demo.dto.TripDeliveryDetailsResponse;
import com.example.demo.dto.TripRetrieveResponse;
import com.example.demo.entity.ActiveRecord;
import com.example.demo.entity.Expenses;
import com.example.demo.entity.Mortality;
import com.example.demo.entity.Payment;
import com.example.demo.entity.SupplierPickup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class TripDeliveryDetailsService {
    private final TripService tripService;
    private final DeliveryService deliveryService;
    private final SupplierPickupService supplierPickupService;
    private final MortalityService mortalityService;
    private final ExpensesService expensesService;
    private final PaymentService paymentService;
    private final Executor tripDeliveryExecutor;

    public TripDeliveryDetailsService(
            TripService tripService,
            DeliveryService deliveryService,
            SupplierPickupService supplierPickupService,
            MortalityService mortalityService,
            ExpensesService expensesService,
            PaymentService paymentService,
            @Qualifier("tripDeliveryExecutor") Executor tripDeliveryExecutor
    ) {
        this.tripService = tripService;
        this.deliveryService = deliveryService;
        this.supplierPickupService = supplierPickupService;
        this.mortalityService = mortalityService;
        this.expensesService = expensesService;
        this.paymentService = paymentService;
        this.tripDeliveryExecutor = tripDeliveryExecutor;
    }

    public Optional<TripDeliveryDetailsResponse> retrieve(Integer tripId) {
        if (tripId == null || tripId <= 0) {
            throw new IllegalArgumentException("Trip ID must be a positive integer.");
        }

        // Resolve the trip first. The other reads are independent and can run concurrently.
        Optional<TripRetrieveResponse> trip = tripService.retrieve(tripId);
        if (trip.isEmpty()) {
            return Optional.empty();
        }

        CompletableFuture<List<DeliveryRetrieveResponse>> deliveriesFuture =
                CompletableFuture.supplyAsync(
                        () -> deliveryService.retrieveByTripId(tripId), tripDeliveryExecutor);

        CompletableFuture<List<Mortality>> mortalitiesFuture =
                CompletableFuture.supplyAsync(
                        () -> mortalityService.findByTripId(tripId), tripDeliveryExecutor);

        CompletableFuture<List<Expenses>> expensesFuture =
                CompletableFuture.supplyAsync(
                        () -> expensesService.findByTripId(tripId), tripDeliveryExecutor);

        CompletableFuture<List<Payment>> paymentsFuture =
                CompletableFuture.supplyAsync(
                        () -> paymentService.findActiveEntitiesByTripId(tripId), tripDeliveryExecutor);

        // Pickup IDs are available only after deliveries have been retrieved.
        CompletableFuture<List<SupplierPickupRetrieveResponse>> pickupsFuture =
                deliveriesFuture.thenCompose(this::retrieveSupplierPickupsAsync);

        TripDeliveryDetailsResponse response = CompletableFuture.allOf(
                        deliveriesFuture, mortalitiesFuture, expensesFuture, paymentsFuture, pickupsFuture)
                .thenApply(ignored -> TripDeliveryDetailsResponse.builder()
                        .trip(trip.get())
                        .deliveries(deliveriesFuture.join())
                        .supplierPickups(pickupsFuture.join())
                        .mortalities(mortalitiesFuture.join())
                        .expenses(expensesFuture.join())
                        .payments(paymentsFuture.join())
                        .build())
                .join();

        return Optional.of(response);
    }

    private CompletableFuture<List<SupplierPickupRetrieveResponse>> retrieveSupplierPickupsAsync(
            List<DeliveryRetrieveResponse> deliveries
    ) {
        LinkedHashSet<Integer> pickupIds = new LinkedHashSet<>();
        for (DeliveryRetrieveResponse delivery : deliveries) {
            SupplierPickup linkedPickup = delivery.getSupplierPickup();
            if (linkedPickup != null && linkedPickup.getId() != null
                    && ActiveRecord.isActiveRecord(linkedPickup)) {
                pickupIds.add(linkedPickup.getId());
            }
        }

        List<CompletableFuture<SupplierPickupRetrieveResponse>> pickupFutures = pickupIds.stream()
                .map(pickupId -> CompletableFuture.supplyAsync(
                                () -> supplierPickupService.retrieve(pickupId), tripDeliveryExecutor)
                        .thenApply(pickup -> pickup
                                .filter(value -> Boolean.TRUE.equals(value.getIsActive())
                                        && !Boolean.TRUE.equals(value.getIsDeleted()))
                                .orElse(null)))
                .toList();

        CompletableFuture<?>[] futures = pickupFutures.toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures)
                .thenApply(ignored -> pickupFutures.stream()
                        .map(CompletableFuture::join)
                        .filter(pickup -> pickup != null)
                        .toList());
    }
}
