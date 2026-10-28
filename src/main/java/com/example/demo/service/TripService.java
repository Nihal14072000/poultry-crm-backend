package com.example.demo.service;

import com.example.demo.entity.Trip;
import com.example.demo.entity.BuyingParty;
import com.example.demo.entity.Employee;
import com.example.demo.entity.SellingParty;
import com.example.demo.entity.TripPartyRelation;
import com.example.demo.entity.Payment;
import com.example.demo.entity.Expenses;
import com.example.demo.dto.BuyingPartydto;
import com.example.demo.dto.SellingPartydto;
import com.example.demo.dto.TripRetrieveResponse;
import com.example.demo.dto.TripVehicleDriversResponse;
import com.example.demo.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class TripService extends CrudService<Trip> {
    private static final String BUYING_PARTY_TYPE = "B";
    private static final String SELLING_PARTY_TYPE = "S";

    private final TripRepository tripRepository;
    private final VehicleService vehicleService;
    private final DriverService driverService;
    private final SupervisorService supervisorService;
    private final BuyingPartyService buyingPartyService;
    private final SellingPartyService sellingPartyService;
    private final TripPartyRelationService tripPartyRelationService;
    private final PartyOrderRelationService partyOrderRelationService;
    private final PaymentService paymentService;
    private final ExpensesService expensesService;

    public TripService(
            TripRepository repository,
            VehicleService vehicleService,
            DriverService driverService,
            SupervisorService supervisorService,
            BuyingPartyService buyingPartyService,
            SellingPartyService sellingPartyService,
            TripPartyRelationService tripPartyRelationService,
            PartyOrderRelationService partyOrderRelationService,
            PaymentService paymentService,
            ExpensesService expensesService
    ) {
        super(repository, Trip::setTripId);
        this.tripRepository = repository;
        this.vehicleService = vehicleService;
        this.driverService = driverService;
        this.supervisorService = supervisorService;
        this.buyingPartyService = buyingPartyService;
        this.sellingPartyService = sellingPartyService;
        this.tripPartyRelationService = tripPartyRelationService;
        this.partyOrderRelationService = partyOrderRelationService;
        this.paymentService = paymentService;
        this.expensesService = expensesService;
    }

    @Transactional(readOnly = true)
    public Optional<TripRetrieveResponse> retrieve(Integer tripId) {
        return tripRepository.findById(tripId)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<List<Employee>> findDriversAssignedToVehicleForTrip(Integer tripId) {
        return tripRepository.findById(tripId)
                .map(trip -> trip.getVehicleId() == null
                        ? List.of()
                        : driverService.findAssignedToVehicle(trip.getVehicleId()));
    }

    @Transactional(readOnly = true)
    public List<TripVehicleDriversResponse> findVehicleDriversForAllTrips() {
        return tripRepository.findAll().stream()
                .map(trip -> TripVehicleDriversResponse.builder()
                        .tripId(trip.getTripId())
                        .vehicle(vehicleService.findById(trip.getVehicleId()).orElse(null))
                        .driver(driverService.findById(trip.getDriverId()).orElse(null))
                        .build())
                .toList();
    }

    @Transactional
    public TripRetrieveResponse createTrip(TripRetrieveResponse request) {
        Trip trip = tripRepository.save(toTrip(request));
        saveParties(trip.getTripId(), request);
        saveFinanceDetails(trip.getTripId(), request);
        return toResponse(trip);
    }

    @Transactional
    public Optional<TripRetrieveResponse> updateTrip(Integer tripId, TripRetrieveResponse request) {
        if (!tripRepository.existsById(tripId)) {
            return Optional.empty();
        }

        Trip trip = toTrip(request);
        trip.setTripId(tripId);
        Trip savedTrip = tripRepository.save(trip);
        saveParties(tripId, request);
        return Optional.of(toResponse(savedTrip));
    }

    private Trip toTrip(TripRetrieveResponse request) {
        Trip trip = new Trip();
        if (request.getVehicle() != null) {
            trip.setVehicleId(vehicleService.create(request.getVehicle()).getId());
        }
        if (request.getDriver() != null) {
            trip.setDriverId(driverService.create(request.getDriver()).getId());
        }
        if (request.getSupervisor() != null) {
            trip.setSupervisorId(supervisorService.create(request.getSupervisor()).getId());
        }
        trip.setRoute(request.getRoute());
        trip.setDate(request.getDate());
        trip.setPickupId(request.getPickupId());
        trip.setDropId(request.getDropId());
        trip.setStatus(request.getStatus());
        trip.setCreateDate(request.getCreateDate());
        trip.setUpdatedDate(request.getUpdatedDate());
        trip.setCreatedBy(request.getCreatedBy());
        trip.setUpdatedBy(request.getUpdatedBy());
        return trip;
    }

    private void saveParties(Integer tripId, TripRetrieveResponse request) {
        List<Integer> buyingPartyIds = request.getBuyingParties() == null ? null
                : request.getBuyingParties().stream()
                        .map(partyRequest -> {
                            BuyingParty party = saveBuyingParty(partyRequest);
                            partyOrderRelationService.replaceOrders(
                                    party.getId(), partyRequest.getOrder(), BUYING_PARTY_TYPE);
                            return party.getId();
                        })
                        .toList();
        List<Integer> sellingPartyIds = request.getSellingParties() == null ? null
                : request.getSellingParties().stream()
                        .map(partyRequest -> {
                            SellingParty party = saveSellingParty(partyRequest);
                            partyOrderRelationService.replaceOrders(
                                    party.getId(), partyRequest.getOrder(), SELLING_PARTY_TYPE);
                            return party.getId();
                        })
                        .toList();
        tripPartyRelationService.replaceParties(tripId, buyingPartyIds, sellingPartyIds);
    }

    private void saveFinanceDetails(Integer tripId, TripRetrieveResponse request) {
        for (Payment payment : request.getPayments() == null ? List.<Payment>of() : request.getPayments()) {
            payment.setId(null);
            payment.setTripId(tripId);
            paymentService.create(payment);
        }
        for (Expenses expense : request.getExpenses() == null ? List.<Expenses>of() : request.getExpenses()) {
            if (expense.getAmount() == null || expense.getAmount().signum() <= 0 || expense.getExpenseDate() == null)
                throw new IllegalArgumentException("Each expense needs a positive amount and date");
            expense.setId(null);
            expense.setTripId(tripId);
            if (expense.getCreateDate() == null) expense.setCreateDate(LocalDateTime.now());
            expensesService.create(expense);
        }
    }

    private BuyingParty saveBuyingParty(BuyingPartydto request) {
        BuyingParty party = new BuyingParty();
        party.setName(request.getName());
        party.setContact(request.getContact());
        party.setLocation(request.getLocation());
        party.setIsActive(request.getIsActive());
        party.setIsDeleted(request.getIsDeleted());
        party.setCreateDate(request.getCreateDate());
        party.setUpdatedDate(request.getUpdatedDate());
        party.setCreatedBy(request.getCreatedBy());
        party.setUpdatedBy(request.getUpdatedBy());

        if (request.getId() == null) {
            return buyingPartyService.create(party);
        }
        return buyingPartyService.update(request.getId(), party)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Buying party not found: " + request.getId()));
    }

    private SellingParty saveSellingParty(SellingPartydto request) {
        SellingParty party = new SellingParty();
        party.setName(request.getName());
        party.setContact(request.getContact());
        party.setLocation(request.getLocation());
        party.setIsActive(request.getIsActive());
        party.setIsDeleted(request.getIsDeleted());
        party.setCreateDate(request.getCreateDate());
        party.setUpdatedDate(request.getUpdatedDate());
        party.setCreatedBy(request.getCreatedBy());
        party.setUpdatedBy(request.getUpdatedBy());

        if (request.getId() == null) {
            return sellingPartyService.create(party);
        }
        return sellingPartyService.update(request.getId(), party)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Selling party not found: " + request.getId()));
    }

    private TripRetrieveResponse toResponse(Trip trip) {
        List<TripPartyRelation> relations =
                tripPartyRelationService.findActiveByTripId(trip.getTripId());
        return TripRetrieveResponse.builder()
                .tripId(trip.getTripId())
                .vehicle(trip.getVehicleId() == null ? null : vehicleService.findById(trip.getVehicleId()).orElse(null))
                .driver(trip.getDriverId() == null ? null : driverService.findById(trip.getDriverId()).orElse(null))
                .supervisor(trip.getSupervisorId() == null ? null : supervisorService.findById(trip.getSupervisorId()).orElse(null))
                .buyingParties(relations.stream()
                        .filter(relation -> BUYING_PARTY_TYPE.equals(relation.getPartyType()))
                        .map(this::toBuyingPartyDto)
                        .toList())
                .sellingParties(relations.stream()
                        .filter(relation -> SELLING_PARTY_TYPE.equals(relation.getPartyType()))
                        .map(this::toSellingPartyDto)
                        .toList())
                .route(trip.getRoute())
                .date(trip.getDate())
                .pickupId(trip.getPickupId())
                .dropId(trip.getDropId())
                .status(trip.getStatus())
                .createDate(trip.getCreateDate())
                .updatedDate(trip.getUpdatedDate())
                .createdBy(trip.getCreatedBy())
                .updatedBy(trip.getUpdatedBy())
                .build();
    }

    private BuyingPartydto toBuyingPartyDto(TripPartyRelation relation) {
        BuyingParty party = buyingPartyService.findById(relation.getPartyId())
                .orElseThrow(() -> new IllegalStateException(
                        "Buying party not found for trip-party relation " + relation.getRelationId()));
        return BuyingPartydto.builder()
                .id(party.getId())
                .name(party.getName())
                .contact(party.getContact())
                .location(party.getLocation())
                .order(partyOrderRelationService.findActiveOrders(party.getId(), BUYING_PARTY_TYPE))
                .isActive(party.getIsActive())
                .isDeleted(party.getIsDeleted())
                .createDate(party.getCreateDate())
                .updatedDate(party.getUpdatedDate())
                .createdBy(party.getCreatedBy())
                .updatedBy(party.getUpdatedBy())
                .build();
    }

    private SellingPartydto toSellingPartyDto(TripPartyRelation relation) {
        SellingParty party = sellingPartyService.findById(relation.getPartyId())
                .orElseThrow(() -> new IllegalStateException(
                        "Selling party not found for trip-party relation " + relation.getRelationId()));
        return SellingPartydto.builder()
                .id(party.getId())
                .name(party.getName())
                .contact(party.getContact())
                .location(party.getLocation())
                .order(partyOrderRelationService.findActiveOrders(party.getId(), SELLING_PARTY_TYPE))
                .isActive(party.getIsActive())
                .isDeleted(party.getIsDeleted())
                .createDate(party.getCreateDate())
                .updatedDate(party.getUpdatedDate())
                .createdBy(party.getCreatedBy())
                .updatedBy(party.getUpdatedBy())
                .build();
    }
}
