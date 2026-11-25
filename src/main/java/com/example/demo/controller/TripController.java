package com.example.demo.controller;

import com.example.demo.entity.Employee;
import com.example.demo.entity.Trip;
import com.example.demo.dto.TripVehicleDriversResponse;
import com.example.demo.dto.TripRetrieveResponse;
import com.example.demo.service.TripService;
import com.example.demo.service.TripDeliveryDetailsService;
import com.example.demo.dto.TripDeliveryDetailsResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {
    private final TripService service;
    private final ObjectMapper objectMapper;
    private final TripDeliveryDetailsService tripDeliveryDetailsService;

    public TripController(
            TripService service,
            ObjectMapper objectMapper,
            TripDeliveryDetailsService tripDeliveryDetailsService
    ) {
        this.service = service;
        this.objectMapper = objectMapper;
        this.tripDeliveryDetailsService = tripDeliveryDetailsService;
    }

    public record PagedTripsResponse(
            List<Trip> items,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}

    @GetMapping
    public List<Trip> findAll() {
        return service.findAll();
    }

    @GetMapping("/paged")
    public PagedTripsResponse findPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer driverId,
            @RequestParam(required = false) Integer supervisorId
    ) {
        var result = service.findPage(page, size, driverId, supervisorId);
        return new PagedTripsResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> findById(@PathVariable Integer id) {
        return service.findById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Trip> create(@RequestBody Trip trip) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(trip));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trip> update(@PathVariable Integer id, @RequestBody Trip trip) {
        return service.update(id, trip).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!service.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/retrieve")
    public ResponseEntity<TripRetrieveResponse> retrieve(@PathVariable Integer id) {
        return service.retrieve(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/delivery-details")
    public ResponseEntity<TripDeliveryDetailsResponse> retrieveDeliveryDetails(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return tripDeliveryDetailsService.retrieve(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/vehicle-drivers")
    public ResponseEntity<List<Employee>> findVehicleDrivers(@PathVariable Integer id) {
        return service.findDriversAssignedToVehicleForTrip(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/vehicle-drivers")
    public List<TripVehicleDriversResponse> findVehicleDriversForAllTrips() {
        return service.findVehicleDriversForAllTrips();
    }

    @PostMapping("/insert")
    public ResponseEntity<TripRetrieveResponse> insert(@RequestBody TripRetrieveResponse trip) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTrip(trip));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<TripRetrieveResponse> update(
            @PathVariable Integer id,
            @RequestBody String rawJson
    ) throws IOException {
        JsonNode request = objectMapper.readTree(rawJson);
        TripRetrieveResponse current = service.retrieve(id).orElse(null);
        if (current == null) {
            return ResponseEntity.notFound().build();
        }

        // Merge supplied JSON properties over the current representation. Omitted
        // properties retain their values; explicitly supplied empty arrays still
        // replace the corresponding relationships.
        TripRetrieveResponse merged = objectMapper.readerForUpdating(current)
                .readValue(request.traverse(objectMapper));
        // Preserve the distinction between an omitted list and an explicitly
        // supplied empty list so untouched relationships are not rewritten.
        if (!request.has("buyingParties")) {
            merged.setBuyingParties(null);
        }
        if (!request.has("sellingParties")) {
            merged.setSellingParties(null);
        }
        return service.updateTrip(id, merged).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
