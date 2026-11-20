package com.example.demo.service;

import com.example.demo.entity.Trip;
import com.example.demo.entity.BuyingParty;
import com.example.demo.entity.Employee;
import com.example.demo.entity.Order;
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

@Service
public class TripService extends CrudService<Trip> {
    private static final String BUYING_PARTY_TYPE = "B";
    private static final String SELLING_PARTY_TYPE = "S";

    private final TripRepository tripRepository;
    private final VehicleService vehicleService;
    private final DriverService driverService;
    private final SupervisorService supervisorService;
    private final EmployeeService employeeService;
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
            EmployeeService employeeService,
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
        this.employeeService = employeeService;
        this.buyingPartyService = buyingPartyService;
        this.sellingPartyService = sellingPartyService;
        this.tripPartyRelationService = tripPartyRelationService;
        this.partyOrderRelationService = partyOrderRelationService;
        this.paymentService = paymentService;
        this.expensesService = expensesService;
    }

    @Transactional(readOnly = true)
    public Optional<TripRetrieveResponse> retrieve(Integer tripId) {
        return findById(tripId)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<List<Employee>> findDriversAssignedToVehicleForTrip(Integer tripId) {
        return findById(tripId)
                .map(trip -> trip.getVehicleId() == null
                        ? List.of()
                        : driverService.findAssignedToVehicle(trip.getVehicleId()));
    }

    @Transactional(readOnly = true)
    public List<TripVehicleDriversResponse> findVehicleDriversForAllTrips() {
        return findAll().stream()
                .map(trip -> TripVehicleDriversResponse.builder()
                        .tripId(trip.getTripId())
                        .vehicle(vehicleService.findById(trip.getVehicleId()).orElse(null))
                        .driver(driverService.findById(trip.getDriverId()).orElse(null))
                        .build())
                .toList();
    }

    @Transactional
    public TripRetrieveResponse createTrip(TripRetrieveResponse request) {
        Trip trip = toTrip(request);
        AuditSupport.markCreated(trip);
        trip = tripRepository.save(trip);
        saveParties(trip.getTripId(), request, false);
        saveFinanceDetails(trip.getTripId(), request);
        return toResponse(trip);
    }

    @Transactional
    public Optional<TripRetrieveResponse> updateTrip(Integer tripId, TripRetrieveResponse request) {
        Trip existingTrip = tripRepository.findById(tripId).orElse(null);
        if (existingTrip == null) {
            return Optional.empty();
        }

        Trip trip = toTrip(request);
        trip.setTripId(tripId);
        AuditSupport.markUpdated(trip, existingTrip);
        Trip savedTrip = tripRepository.save(trip);
        saveParties(tripId, request, true);
        return Optional.of(toResponse(savedTrip));
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        if (!tripRepository.existsById(id)) {
            return false;
        }
        partyOrderRelationService.deleteOrdersByTripId(id);
        paymentService.deleteByTripId(id);
        tripRepository.deleteById(id);
        return true;
    }

    private Trip toTrip(TripRetrieveResponse request) {
        Trip trip = new Trip();
        if (request.getVehicle() != null) {
            trip.setVehicleId(request.getVehicle().getId());
        }
        if (request.getDriver() != null) {
            trip.setDriverId(request.getDriver().getId());
        }
        if (request.getSupervisor() != null) {
            trip.setSupervisorId(request.getSupervisor().getId());
        }
        if (request.getLabour() != null) {
            trip.setLabourId(request.getLabour().getId());
        }
        trip.setRoute(request.getRoute());
        trip.setDate(request.getDate());
        trip.setPickupId(request.getPickupId());
        trip.setDropId(request.getDropId());
        trip.setStatus(request.getStatus());
        return trip;
    }

    private void saveParties(Integer tripId, TripRetrieveResponse request, boolean replaceExistingOrders) {
        List<Integer> buyingPartyIds = request.getBuyingParties() == null ? null
                : request.getBuyingParties().stream()
                        .map(partyRequest -> {
                            BuyingParty party = saveBuyingParty(partyRequest);
                            savePartyOrders(
                                    tripId, party.getId(), partyRequest.getOrder(),
                                    BUYING_PARTY_TYPE, replaceExistingOrders);
                            return party.getId();
                        })
                        .toList();
        List<Integer> sellingPartyIds = request.getSellingParties() == null ? null
                : request.getSellingParties().stream()
                        .map(partyRequest -> {
                            SellingParty party = saveSellingParty(partyRequest);
                            savePartyOrders(
                                    tripId, party.getId(), partyRequest.getOrder(),
                                    SELLING_PARTY_TYPE, replaceExistingOrders);
                            return party.getId();
                        })
                        .toList();
        tripPartyRelationService.replaceParties(tripId, buyingPartyIds, sellingPartyIds);
    }

    private void savePartyOrders(
            Integer tripId,
            Integer partyId,
            List<Order> orders,
            String partyType,
            boolean replaceExistingOrders
    ) {
        if (replaceExistingOrders) {
            partyOrderRelationService.replaceOrders(partyId, orders, partyType, tripId);
        } else {
            partyOrderRelationService.addOrders(partyId, orders, partyType, tripId);
        }
    }

    private void saveFinanceDetails(Integer tripId, TripRetrieveResponse request) {
        for (Payment requested : request.getPayments() == null ? List.<Payment>of() : request.getPayments()) {
            Payment payment = new Payment();
            payment.setBuyingPartyId(requested.getBuyingPartyId());
            payment.setActualOrderId(requested.getActualOrderId());
            payment.setTripId(tripId);
            payment.setNote(requested.getNote());
            payment.setPaymentType(requested.getPaymentType());
            payment.setAmount(requested.getAmount());
            payment.setPaymentDate(requested.getPaymentDate());
            payment.setPaymentMethod(requested.getPaymentMethod());
            payment.setExternalReference(requested.getExternalReference());
            payment.setSellingPartyId(requested.getSellingPartyId());
            payment.setStatus(requested.getStatus());
            paymentService.create(payment);
        }
        for (Expenses requested : request.getExpenses() == null ? List.<Expenses>of() : request.getExpenses()) {
            if (requested.getAmount() == null || requested.getAmount().signum() <= 0 || requested.getExpenseDate() == null) {
                throw new IllegalArgumentException("Each expense needs a positive amount and date");
            }
            Expenses expense = new Expenses();
            expense.setTripId(tripId);
            expense.setAmount(requested.getAmount());
            expense.setExpenseDate(requested.getExpenseDate());
            expense.setCategory(requested.getCategory());
            expense.setSellingPartyId(requested.getSellingPartyId());
            expense.setActualOrderId(requested.getActualOrderId());
            expense.setNote(requested.getNote());
            expensesService.create(expense);
        }
    }

    private BuyingParty saveBuyingParty(BuyingPartydto request) {
        if (request.getId() == null) {
            BuyingParty party = new BuyingParty();
            party.setName(request.getName());
            party.setContact(request.getContact());
            party.setLocation(request.getLocation());
            return buyingPartyService.create(party);
        }

        BuyingParty party = buyingPartyService.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Buying party not found: " + request.getId()));
        if (request.getName() != null) party.setName(request.getName());
        if (request.getContact() != null) party.setContact(request.getContact());
        if (request.getLocation() != null) party.setLocation(request.getLocation());
        return buyingPartyService.update(request.getId(), party)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Buying party not found: " + request.getId()));
    }

    private SellingParty saveSellingParty(SellingPartydto request) {
        if (request.getId() == null) {
            SellingParty party = new SellingParty();
            party.setName(request.getName());
            party.setContact(request.getContact());
            party.setLocation(request.getLocation());
            return sellingPartyService.create(party);
        }

        SellingParty party = sellingPartyService.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Selling party not found: " + request.getId()));
        if (request.getName() != null) party.setName(request.getName());
        if (request.getContact() != null) party.setContact(request.getContact());
        if (request.getLocation() != null) party.setLocation(request.getLocation());
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
                .labour(trip.getLabourId() == null ? null : employeeService.findById(trip.getLabourId()).orElse(null))
                .buyingParties(relations.stream()
                        .filter(relation -> BUYING_PARTY_TYPE.equals(relation.getPartyType()))
                        .map(relation -> toBuyingPartyDto(trip.getTripId(), relation))
                        .toList())
                .sellingParties(relations.stream()
                        .filter(relation -> SELLING_PARTY_TYPE.equals(relation.getPartyType()))
                        .map(relation -> toSellingPartyDto(trip.getTripId(), relation))
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

    private BuyingPartydto toBuyingPartyDto(Integer tripId, TripPartyRelation relation) {
        BuyingParty party = buyingPartyService.findById(relation.getPartyId()).get();
        return BuyingPartydto.builder()
                .id(party.getId())
                .name(party.getName())
                .contact(party.getContact())
                .location(party.getLocation())
                .order(partyOrderRelationService.findActiveOrders(
                        tripId, party.getId(), BUYING_PARTY_TYPE))
                .isActive(party.getIsActive())
                .isDeleted(party.getIsDeleted())
                .createDate(party.getCreateDate())
                .updatedDate(party.getUpdatedDate())
                .createdBy(party.getCreatedBy())
                .updatedBy(party.getUpdatedBy())
                .build();
    }

    private SellingPartydto toSellingPartyDto(Integer tripId, TripPartyRelation relation) {
        SellingParty party = sellingPartyService.findById(relation.getPartyId())
                .orElseThrow(() -> new IllegalStateException(
                        "Selling party not found for trip-party relation " + relation.getRelationId()));
        return SellingPartydto.builder()
                .id(party.getId())
                .name(party.getName())
                .contact(party.getContact())
                .location(party.getLocation())
                .order(partyOrderRelationService.findActiveOrders(
                        tripId, party.getId(), SELLING_PARTY_TYPE))
                .isActive(party.getIsActive())
                .isDeleted(party.getIsDeleted())
                .createDate(party.getCreateDate())
                .updatedDate(party.getUpdatedDate())
                .createdBy(party.getCreatedBy())
                .updatedBy(party.getUpdatedBy())
                .build();
    }
}
