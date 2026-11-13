package com.example.demo.service;

import com.example.demo.dto.PaymentRetrieveResponse;
import com.example.demo.dto.BuyingPartydto;
import com.example.demo.dto.SellingPartydto;
import com.example.demo.dto.TripRetrieveResponse;
import com.example.demo.entity.BuyingParty;
import com.example.demo.entity.ActiveRecord;
import com.example.demo.entity.Employee;
import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import com.example.demo.entity.SellingParty;
import com.example.demo.entity.Trip;
import com.example.demo.repository.BuyingPartyRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.FinancePaymentAllocationRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.SellingPartyRepository;
import com.example.demo.repository.TripRepository;
import com.example.demo.repository.VehicleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PaymentService extends CrudService<Payment> {
    private final PaymentRepository repository;
    private final BuyingPartyRepository buyingPartyRepository;
    private final OrderRepository orderRepository;
    private final TripRepository tripRepository;
    private final SellingPartyRepository sellingPartyRepository;
    private final FinancePaymentAllocationRepository allocationRepository;
    private final VehicleRepository vehicleRepository;
    private final EmployeeRepository employeeRepository;
    private final TripPartyRelationService tripPartyRelationService;
    private final PartyOrderRelationService partyOrderRelationService;

    public PaymentService(
            PaymentRepository repository,
            BuyingPartyRepository buyingPartyRepository,
            OrderRepository orderRepository,
            TripRepository tripRepository,
            SellingPartyRepository sellingPartyRepository,
            FinancePaymentAllocationRepository allocationRepository,
            VehicleRepository vehicleRepository,
            EmployeeRepository employeeRepository,
            TripPartyRelationService tripPartyRelationService,
            PartyOrderRelationService partyOrderRelationService
    ) {
        super(repository, Payment::setId);
        this.repository = repository;
        this.buyingPartyRepository = buyingPartyRepository;
        this.orderRepository = orderRepository;
        this.tripRepository = tripRepository;
        this.sellingPartyRepository = sellingPartyRepository;
        this.allocationRepository = allocationRepository;
        this.vehicleRepository = vehicleRepository;
        this.employeeRepository = employeeRepository;
        this.tripPartyRelationService = tripPartyRelationService;
        this.partyOrderRelationService = partyOrderRelationService;
    }

    @Transactional(readOnly = true)
    public List<PaymentRetrieveResponse> retrieveAll() {
        return findAll().stream()
                .map(this::toRetrieveResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<PaymentRetrieveResponse> retrieve(Integer id) {
        return findById(id).map(this::toRetrieveResponse);
    }

    private PaymentRetrieveResponse toRetrieveResponse(Payment payment) {
        try{
            BuyingPartydto buyingParty = payment.getBuyingPartyId() == null
                    ? null
                    : buyingPartyRepository.findById(payment.getBuyingPartyId())
                    .filter(ActiveRecord::isActiveRecord)
                    .map(this::toBuyingPartyDto)
                    .orElse(null);
            Order actualOrder = payment.getActualOrderId() == null
                    ? null
                    : orderRepository.findById(payment.getActualOrderId())
                    .filter(ActiveRecord::isActiveRecord).orElse(null);
            TripRetrieveResponse trip = payment.getTripId() == null
                    ? null
                    : tripRepository.findById(payment.getTripId())
                    .map(this::toTripRetrieveResponse)
                    .orElse(null);
            SellingPartydto sellingParty = payment.getSellingPartyId() == null
                    ? null
                    : sellingPartyRepository.findById(payment.getSellingPartyId())
                    .filter(ActiveRecord::isActiveRecord)
                    .map(this::toSellingPartyDto)
                    .orElse(null);

            return PaymentRetrieveResponse.from(
                    payment,
                    buyingParty,
                    actualOrder,
                    trip,
                    sellingParty,
                    allocationRepository.findByPaymentId(payment.getId())
            );

        } catch (Exception e) {
            log.info("Error retrieving payment with ID {}: {}", payment.getId(), e.getMessage());
            return null;
        }



    }

    private TripRetrieveResponse toTripRetrieveResponse(Trip trip) {
        var relations = tripPartyRelationService.findActiveByTripId(trip.getTripId());
        return TripRetrieveResponse.builder()
                .tripId(trip.getTripId())
                //.vehicle(trip.getVehicleId() == null
                 //       ? null
                //: vehicleRepository.findById(trip.getVehicleId()).orElse(null))
                //.driver(findEmployee(trip.getDriverId()))
                //.supervisor(findEmployee(trip.getSupervisorId()))
                .buyingParties(relations.stream()
                        .filter(relation -> "B".equals(relation.getPartyType()))
                        .map(relation -> buyingPartyRepository.findById(relation.getPartyId())
                                .filter(ActiveRecord::isActiveRecord)
                                .map(this::toBuyingPartyDto)
                                .orElseThrow(() -> new IllegalStateException(
                                        "Buying party not found for trip-party relation "
                                                + relation.getRelationId())))
                        .toList())
                .sellingParties(relations.stream()
                        .filter(relation -> "S".equals(relation.getPartyType()))
                        .map(relation -> sellingPartyRepository.findById(relation.getPartyId())
                                .filter(ActiveRecord::isActiveRecord)
                                .map(this::toSellingPartyDto)
                                .orElseThrow(() -> new IllegalStateException(
                                        "Selling party not found for trip-party relation "
                                                + relation.getRelationId())))
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

    private Employee findEmployee(Integer employeeId) {
        return employeeId == null ? null : employeeRepository.findById(employeeId).orElse(null);
    }

    private BuyingPartydto toBuyingPartyDto(BuyingParty party) {
        return BuyingPartydto.builder()
                .id(party.getId())
                .name(party.getName())
                .contact(party.getContact())
                .location(party.getLocation())
                .order(partyOrderRelationService.findActiveOrders(party.getId(), "B"))
                .isActive(party.getIsActive())
                .isDeleted(party.getIsDeleted())
                .createDate(party.getCreateDate())
                .updatedDate(party.getUpdatedDate())
                .createdBy(party.getCreatedBy())
                .updatedBy(party.getUpdatedBy())
                .build();
    }

    private SellingPartydto toSellingPartyDto(SellingParty party) {
        return SellingPartydto.builder()
                .id(party.getId())
                .name(party.getName())
                .contact(party.getContact())
                .location(party.getLocation())
                .order(partyOrderRelationService.findActiveOrders(party.getId(), "S"))
                .isActive(party.getIsActive())
                .isDeleted(party.getIsDeleted())
                .createDate(party.getCreateDate())
                .updatedDate(party.getUpdatedDate())
                .createdBy(party.getCreatedBy())
                .updatedBy(party.getUpdatedBy())
                .build();
    }

    @Override
    @Transactional
    public Payment create(Payment payment) {
        validate(payment);
        if (payment.getCreateDate() == null) payment.setCreateDate(java.time.LocalDateTime.now());
        if (payment.getStatus() == null) payment.setStatus("PENDING");
        return repository.save(payment);
    }

    @Override
    @Transactional
    public java.util.Optional<Payment> update(Integer id, Payment payment) {
        if (!repository.existsById(id)) return java.util.Optional.empty();
        validate(payment);
        payment.setId(id);
        payment.setUpdatedDate(java.time.LocalDateTime.now());
        return java.util.Optional.of(repository.save(payment));
    }

    private void validate(Payment payment) {
        boolean hasBuyingParty = payment.getBuyingPartyId() != null;
        boolean hasSellingParty = payment.getSellingPartyId() != null;
        if (hasBuyingParty && hasSellingParty)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment must reference only one party");
        if (hasBuyingParty) {
            payment.setPaymentType("BUYER_RECEIPT");
        } else if (hasSellingParty) {
            payment.setPaymentType("SELLER_PAYOUT");
        } else if (payment.getPaymentType() == null) {
            return; // Allows legacy payment records.
        }

        boolean buyer = "BUYER_RECEIPT".equals(payment.getPaymentType());
        boolean seller = "SELLER_PAYOUT".equals(payment.getPaymentType());
        if (!buyer && !seller) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment type");
        if (payment.getAmount() == null || payment.getAmount().signum() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount must be positive");
        if (buyer && (payment.getBuyingPartyId() == null || payment.getSellingPartyId() != null)
                || seller && (payment.getSellingPartyId() == null || payment.getBuyingPartyId() != null))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment must reference only the party for its type");
        if (payment.getStatus() != null && !java.util.List.of("PENDING", "COMPLETED", "FAILED", "VOID").contains(payment.getStatus()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment status");
        if (payment.getPaymentMethod() != null && !java.util.List.of("BANK_TRANSFER", "CASH", "CHEQUE", "UPI", "CARD", "OTHER").contains(payment.getPaymentMethod()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment method");
    }
}
