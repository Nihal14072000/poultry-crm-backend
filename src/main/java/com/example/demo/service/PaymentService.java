package com.example.demo.service;

import com.example.demo.dto.PaymentRetrieveResponse;
import com.example.demo.dto.BuyingPartydto;
import com.example.demo.dto.PaymentSumResponse;
import com.example.demo.dto.SellingPartydto;
import com.example.demo.dto.TripRetrieveResponse;
import com.example.demo.entity.BuyingParty;
import com.example.demo.entity.ActiveRecord;
import com.example.demo.entity.CreatedRecord;
import com.example.demo.entity.Employee;
import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import com.example.demo.entity.PartyOrderRelation;
import com.example.demo.entity.TripPartyRelation;
import com.example.demo.entity.SellingParty;
import com.example.demo.entity.Trip;
import com.example.demo.repository.BuyingPartyRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.FinancePaymentAllocationRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.PartyOrderRelationRepository;
import com.example.demo.repository.SellingPartyRepository;
import com.example.demo.repository.TripRepository;
import com.example.demo.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
public class PaymentService extends CrudService<Payment> {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository repository;
    private final PartyOrderRelationRepository partyOrderRelationRepository;
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
            PartyOrderRelationRepository partyOrderRelationRepository,
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
        this.partyOrderRelationRepository = partyOrderRelationRepository;
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
    public com.example.demo.dto.PagedResponse<PaymentRetrieveResponse> retrievePaged(
            int page, int size, Integer tripId, String status, String paymentType, Integer partyId, String query) {
        var pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Order.desc("createDate").nullsLast(),
                        org.springframework.data.domain.Sort.Order.desc("id")));
        String q = (query == null || query.isBlank()) ? null : "%" + query.trim().toLowerCase() + "%";
        String normStatus = (status == null || status.isBlank()) ? null : status.trim();
        String normType = (paymentType == null || paymentType.isBlank()) ? null : paymentType.trim();
        var result = repository.findPage(
                com.example.demo.security.TenantContext.requireTenantId(),
                tripId,
                normStatus,
                normType,
                partyId,
                q,
                pageable);
        return com.example.demo.dto.PagedResponse.from(result.map(this::toRetrieveResponse));
    }

    @Transactional(readOnly = true)
    public List<PaymentRetrieveResponse> retrieveByParty(Integer partyId, String partyTyp) {
        List<Payment> payments;
        if ("B".equals(partyTyp)) {
            payments = repository.findByBuyingPartyId(partyId);
        } else if ("S".equals(partyTyp)) {
            payments = repository.findBySellingPartyId(partyId);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported party type");
        }

        long tenantId = com.example.demo.security.TenantContext.requireTenantId();
        payments = CreatedRecord.newestFirst(payments.stream()
                .filter(ActiveRecord::isActiveRecord)
                .filter(p -> java.util.Objects.equals(p.getTenantId(), tenantId))
                .toList());
        log.info("Retrieved {} active payments for partyId={}, partyTyp={}",
                payments.size(), partyId, partyTyp);
        return payments.stream().map(this::toRetrieveResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaymentSumResponse sumCompletedPaymentsByParty(
            Integer partyId,
            String partyType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (partyId == null || partyId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Party ID must be positive");
        }
        if (!"B".equals(partyType) && !"S".equals(partyType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported party type");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment date range");
        }

        BigDecimal total = repository.sumCompletedPaymentsByParty(
                com.example.demo.security.TenantContext.requireTenantId(), partyId, partyType, startDate, endDate);
        return new PaymentSumResponse(
                partyId,
                partyType,
                startDate,
                endDate,
                total == null ? BigDecimal.ZERO : total
        );
    }

    @Transactional(readOnly = true)
    public List<PaymentRetrieveResponse> retrieveByTripId(Integer tripId) {
        long tenantId = com.example.demo.security.TenantContext.requireTenantId();
        List<Payment> payments = repository.findActiveByTripIdOrderByCreateDateDesc(tripId).stream()
                .filter(p -> java.util.Objects.equals(p.getTenantId(), tenantId))
                .toList();
        if (payments.isEmpty()) {
            log.info("Retrieved 0 active payments for tripId={}", tripId);
            return List.of();
        }

        // Load the trip graph once, rather than rebuilding it for every payment.
        Trip trip = tripRepository.findById(tripId).orElse(null);
        List<TripPartyRelation> tripRelations = trip == null
                ? List.of()
                : tripPartyRelationService.findActiveByTripId(tripId);

        Set<Integer> buyingPartyIds = payments.stream()
                .map(Payment::getBuyingPartyId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(java.util.HashSet::new));
        Set<Integer> sellingPartyIds = payments.stream()
                .map(Payment::getSellingPartyId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(java.util.HashSet::new));
        tripRelations.stream()
                .filter(relation -> "B".equals(relation.getPartyType()))
                .map(TripPartyRelation::getPartyId)
                .forEach(buyingPartyIds::add);
        tripRelations.stream()
                .filter(relation -> "S".equals(relation.getPartyType()))
                .map(TripPartyRelation::getPartyId)
                .forEach(sellingPartyIds::add);

        Map<Integer, BuyingParty> buyingParties = new HashMap<>();
        if (!buyingPartyIds.isEmpty()) {
            new ArrayList<>(buyingPartyRepository.findAllById(buyingPartyIds)).stream()
                    .filter(ActiveRecord::isActiveRecord)
                    .forEach(party -> buyingParties.put(party.getId(), party));
        }
        Map<Integer, SellingParty> sellingParties = new HashMap<>();
        if (!sellingPartyIds.isEmpty()) {
            new ArrayList<>(sellingPartyRepository.findAllById(sellingPartyIds)).stream()
                    .filter(ActiveRecord::isActiveRecord)
                    .forEach(party -> sellingParties.put(party.getId(), party));
        }

        List<PartyOrderRelation> buyingOrderRelations = buyingPartyIds.isEmpty()
                ? List.of()
                : partyOrderRelationRepository.findActiveByPartyIdsAndPartyType(
                        tenantId, buyingPartyIds, "B");
        List<PartyOrderRelation> sellingOrderRelations = sellingPartyIds.isEmpty()
                ? List.of()
                : partyOrderRelationRepository.findActiveByPartyIdsAndPartyType(
                        tenantId, sellingPartyIds, "S");

        Set<Integer> orderIds = payments.stream()
                .map(Payment::getActualOrderId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(java.util.HashSet::new));
        buyingOrderRelations.stream().map(PartyOrderRelation::getOrderId).forEach(orderIds::add);
        sellingOrderRelations.stream().map(PartyOrderRelation::getOrderId).forEach(orderIds::add);

        Map<Integer, Order> ordersById = new HashMap<>();
        if (!orderIds.isEmpty()) {
            new ArrayList<>(orderRepository.findAllById(orderIds)).stream()
                    .filter(ActiveRecord::isActiveRecord)
                    .forEach(order -> ordersById.put(order.getId(), order));
        }
        Map<Integer, List<Order>> buyingOrdersByParty = groupActiveOrdersByParty(
                buyingOrderRelations, ordersById);
        Map<Integer, List<Order>> sellingOrdersByParty = groupActiveOrdersByParty(
                sellingOrderRelations, ordersById);

        TripRetrieveResponse tripResponse = trip == null ? null
                : toTripRetrieveResponse(trip, tripRelations, buyingParties, sellingParties,
                        buyingOrdersByParty, sellingOrdersByParty);

        Set<Integer> paymentIds = payments.stream().map(Payment::getId).collect(Collectors.toSet());
        Map<Integer, List<com.example.demo.entity.FinancePaymentAllocation>> allocationsByPayment =
                allocationRepository.findByPaymentIdIn(paymentIds).stream()
                        .filter(a -> java.util.Objects.equals(a.getTenantId(), tenantId))
                        .collect(Collectors.groupingBy(
                                com.example.demo.entity.FinancePaymentAllocation::getPaymentId));

        log.info("Retrieved {} active payments for tripId={}", payments.size(), tripId);
        return payments.stream().map(payment -> PaymentRetrieveResponse.from(
                payment,
                payment.getBuyingPartyId() == null ? null
                        : toBuyingPartyDto(buyingParties.get(payment.getBuyingPartyId()),
                                buyingOrdersByParty.getOrDefault(payment.getBuyingPartyId(), List.of())),
                payment.getActualOrderId() == null ? null : ordersById.get(payment.getActualOrderId()),
                tripResponse,
                payment.getSellingPartyId() == null ? null
                        : toSellingPartyDto(sellingParties.get(payment.getSellingPartyId()),
                                sellingOrdersByParty.getOrDefault(payment.getSellingPartyId(), List.of())),
                allocationsByPayment.getOrDefault(payment.getId(), List.of())
        )).toList();
    }

    @Transactional(readOnly = true)
    public List<Payment> findActiveEntitiesByTripId(Integer tripId) {
        long tenantId = com.example.demo.security.TenantContext.requireTenantId();
        return repository.findActiveByTripIdOrderByCreateDateDesc(tripId).stream()
                .filter(p -> java.util.Objects.equals(p.getTenantId(), tenantId))
                .toList();
    }

    private Map<Integer, List<Order>> groupActiveOrdersByParty(
            List<PartyOrderRelation> relations,
            Map<Integer, Order> ordersById
    ) {
        Map<Integer, Map<Integer, Order>> grouped = new HashMap<>();
        for (PartyOrderRelation relation : relations) {
            Order order = ordersById.get(relation.getOrderId());
            if (order != null) {
                grouped.computeIfAbsent(relation.getPartyId(), ignored -> new LinkedHashMap<>())
                        .putIfAbsent(order.getId(), order);
            }
        }
        Map<Integer, List<Order>> result = new HashMap<>();
        grouped.forEach((partyId, orders) -> result.put(
                partyId,
                CreatedRecord.newestFirst(new ArrayList<>(orders.values()))
        ));
        return result;
    }

    private TripRetrieveResponse toTripRetrieveResponse(
            Trip trip,
            List<TripPartyRelation> relations,
            Map<Integer, BuyingParty> buyingParties,
            Map<Integer, SellingParty> sellingParties,
            Map<Integer, List<Order>> buyingOrdersByParty,
            Map<Integer, List<Order>> sellingOrdersByParty
    ) {
        return TripRetrieveResponse.builder()
                .tripId(trip.getTripId())
                .buyingParties(relations.stream()
                        .filter(relation -> "B".equals(relation.getPartyType()))
                        .map(relation -> {
                            BuyingParty party = buyingParties.get(relation.getPartyId());
                            if (party == null) {
                                throw new IllegalStateException("Buying party not found for trip-party relation "
                                        + relation.getRelationId());
                            }
                            return toBuyingPartyDto(party,
                                    buyingOrdersByParty.getOrDefault(party.getId(), List.of()));
                        })
                        .toList())
                .sellingParties(relations.stream()
                        .filter(relation -> "S".equals(relation.getPartyType()))
                        .map(relation -> {
                            SellingParty party = sellingParties.get(relation.getPartyId());
                            if (party == null) {
                                throw new IllegalStateException("Selling party not found for trip-party relation "
                                        + relation.getRelationId());
                            }
                            return toSellingPartyDto(party,
                                    sellingOrdersByParty.getOrDefault(party.getId(), List.of()));
                        })
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

    private BuyingPartydto toBuyingPartyDto(BuyingParty party, List<Order> orders) {
        if (party == null) {
            return null;
        }
        return BuyingPartydto.builder()
                .id(party.getId())
                .name(party.getName())
                .contact(party.getContact())
                .location(party.getLocation())
                .outstanding(party.getOutstanding())
                .order(orders)
                .isActive(party.getIsActive())
                .isDeleted(party.getIsDeleted())
                .createDate(party.getCreateDate())
                .updatedDate(party.getUpdatedDate())
                .createdBy(party.getCreatedBy())
                .updatedBy(party.getUpdatedBy())
                .build();
    }

    private SellingPartydto toSellingPartyDto(SellingParty party, List<Order> orders) {
        if (party == null) {
            return null;
        }
        return SellingPartydto.builder()
                .id(party.getId())
                .name(party.getName())
                .contact(party.getContact())
                .location(party.getLocation())
                .outstanding(party.getOutstanding())
                .order(orders)
                .isActive(party.getIsActive())
                .isDeleted(party.getIsDeleted())
                .createDate(party.getCreateDate())
                .updatedDate(party.getUpdatedDate())
                .createdBy(party.getCreatedBy())
                .updatedBy(party.getUpdatedBy())
                .build();
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

            long tenantId = com.example.demo.security.TenantContext.requireTenantId();
            return PaymentRetrieveResponse.from(
                    payment,
                    buyingParty,
                    actualOrder,
                    trip,
                    sellingParty,
                    allocationRepository.findByPaymentId(payment.getId()).stream()
                            .filter(a -> java.util.Objects.equals(a.getTenantId(), tenantId))
                            .toList()
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
                .outstanding(party.getOutstanding())
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
                .outstanding(party.getOutstanding())
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
        payment.setTenantId(com.example.demo.security.TenantContext.requireTenantId());
        AuditSupport.markCreated(payment);
        if (payment.getStatus() == null) payment.setStatus("PENDING");
        return repository.save(payment);
    }

    @Override
    @Transactional
    public java.util.Optional<Payment> update(Integer id, Payment payment) {
        long tenantId = com.example.demo.security.TenantContext.requireTenantId();
        Payment existing = repository.findById(id)
                .filter(p -> java.util.Objects.equals(p.getTenantId(), tenantId))
                .orElse(null);
        if (existing == null) return java.util.Optional.empty();
        validate(payment);
        payment.setId(id);
        payment.setTenantId(tenantId);
        // These fields are server-managed and must survive an ordinary payment update.
        payment.setIsActive(existing.getIsActive());
        payment.setIsDeleted(existing.getIsDeleted());
        AuditSupport.markUpdated(payment, existing);
        return java.util.Optional.of(repository.save(payment));
    }

    @Transactional
    public void deleteByTripId(Integer tripId) {
        long tenantId = com.example.demo.security.TenantContext.requireTenantId();
        allocationRepository.deleteByPaymentTripId(tenantId, tripId);
        repository.deleteByTripId(tripId);
    }

    private void validate(Payment payment) {
        boolean hasBuyingParty = payment.getBuyingPartyId() != null;
        boolean hasSellingParty = payment.getSellingPartyId() != null;
        if (hasBuyingParty && hasSellingParty)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment must reference only one party");
        // Buying parties are suppliers we pay; selling parties are vendors who pay us.
        if (hasBuyingParty) {
            payment.setPaymentType("SELLER_PAYOUT");
        } else if (hasSellingParty) {
            payment.setPaymentType("BUYER_RECEIPT");
        } else if (payment.getPaymentType() == null) {
            return; // Allows legacy payment records.
        }

        boolean buyer = "BUYER_RECEIPT".equals(payment.getPaymentType());
        boolean seller = "SELLER_PAYOUT".equals(payment.getPaymentType());
        if (!buyer && !seller) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment type");
        if (payment.getAmount() == null || payment.getAmount().signum() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount must be positive");
        if (buyer && (payment.getSellingPartyId() == null || payment.getBuyingPartyId() != null)
                || seller && (payment.getBuyingPartyId() == null || payment.getSellingPartyId() != null))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment must reference only the party for its type");
        if (payment.getStatus() != null && !java.util.List.of("PENDING", "COMPLETED", "FAILED", "VOID").contains(payment.getStatus()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment status");
        if (payment.getPaymentMethod() != null && !java.util.List.of("BANK_TRANSFER", "CASH", "CHEQUE", "UPI", "CARD", "OTHER").contains(payment.getPaymentMethod()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment method");
    }
}
