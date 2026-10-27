package com.example.demo.controller;

import com.example.demo.entity.Employee;
import com.example.demo.entity.Trip;
import com.example.demo.dto.TripVehicleDriversResponse;
import com.example.demo.dto.TripRetrieveResponse;
import com.example.demo.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {
    private final TripService service;

    public TripController(TripService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List trips", description = "Returns all trips.")
    public List<Trip> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a trip", description = "Returns a trip by ID, or 404 if it does not exist.")
    public ResponseEntity<Trip> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a trip", description = "Creates a trip from the supplied trip data.")
    public ResponseEntity<Trip> create(@RequestBody Trip trip) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(trip));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a trip", description = "Replaces a trip by ID, or returns 404 if it does not exist.")
    public ResponseEntity<Trip> update(@PathVariable Integer id, @RequestBody Trip trip) {
        return service.update(id, trip)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a trip", description = "Deletes a trip by ID. Returns 404 if it does not exist.")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!service.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/retrieve")
    @Operation(
            summary = "Retrieve trip details",
            description = "Returns the trip with its related vehicle, driver, supervisor, buying parties, and selling parties. Returns 404 if the trip does not exist."
    )
    public ResponseEntity<TripRetrieveResponse> retrieve(@PathVariable Integer id) {
        return service.retrieve(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/vehicle-drivers")
    @Operation(
            summary = "List drivers assigned to a trip's vehicle",
            description = "Returns distinct drivers assigned to the vehicle used by this trip across all trips. Returns 404 if the trip does not exist."
    )
    public ResponseEntity<List<Employee>> findVehicleDrivers(@PathVariable Integer id) {
        return service.findDriversAssignedToVehicleForTrip(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/vehicle-drivers")
    @Operation(
            summary = "List vehicle drivers for all trips",
            description = "Returns each trip with the distinct drivers assigned to its vehicle across all trips. Trips without a vehicle have an empty drivers list."
    )
    public List<TripVehicleDriversResponse> findVehicleDriversForAllTrips() {
        return service.findVehicleDriversForAllTrips();
    }

    @PostMapping("/insert")
    @Operation(
            summary = "Insert a trip with related parties",
            description = "Creates a trip from the supplied trip details and saves its related vehicle, driver, supervisor, buying parties, and selling parties."
    )
    public ResponseEntity<TripRetrieveResponse> insert(@RequestBody TripRetrieveResponse trip) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTrip(trip));
    }

    @PutMapping("/{id}/update")
    @Operation(
            summary = "Update a trip with related parties",
            description = "Updates a trip and its related vehicle, driver, supervisor, buying parties, and selling parties. Returns 404 if the trip does not exist."
    )
    public ResponseEntity<TripRetrieveResponse> update(
            @PathVariable Integer id,
            @RequestBody TripRetrieveResponse trip
    ) {
        return service.updateTrip(id, trip)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
