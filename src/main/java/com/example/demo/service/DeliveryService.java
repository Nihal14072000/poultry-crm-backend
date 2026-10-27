package com.example.demo.service;

import com.example.demo.dto.DeliveryRetrieveResponse;
import com.example.demo.dto.VendorDeliveryRetrieveResponse;
import com.example.demo.entity.Delivery;
import com.example.demo.entity.DeliveryPartyRelation;
import com.example.demo.entity.Expenses;
import com.example.demo.entity.Mortality;
import com.example.demo.entity.Payment;
import com.example.demo.entity.SellingParty;
import com.example.demo.entity.SupplierPickup;
import com.example.demo.entity.Trip;
import com.example.demo.entity.VendorDelivery;
import com.example.demo.repository.DeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DeliveryService extends CrudService<Delivery> {
    private final DeliveryRepository deliveryRepository;
    private final TripService tripService;
    private final DeliveryPartyRelationService deliveryPartyRelationService;
    private final VendorDeliveryService vendorDeliveryService;
    private final SellingPartyService sellingPartyService;
    private final PartyOrderRelationService partyOrderRelationService;
    private final SupplierPickupService supplierPickupService;
    private final MortalityService mortalityService;
    private final PaymentService paymentService;
    private final ExpensesService expensesService;

    public DeliveryService(
            DeliveryRepository repository,
            TripService tripService,
            DeliveryPartyRelationService deliveryPartyRelationService,
            VendorDeliveryService vendorDeliveryService,
            SellingPartyService sellingPartyService,
            PartyOrderRelationService partyOrderRelationService,
            SupplierPickupService supplierPickupService,
            MortalityService mortalityService,
            PaymentService paymentService,
            ExpensesService expensesService
    ) {
        super(repository, Delivery::setId);
        this.deliveryRepository = repository;
        this.tripService = tripService;
        this.deliveryPartyRelationService = deliveryPartyRelationService;
        this.vendorDeliveryService = vendorDeliveryService;
        this.sellingPartyService = sellingPartyService;
        this.partyOrderRelationService = partyOrderRelationService;
        this.supplierPickupService = supplierPickupService;
        this.mortalityService = mortalityService;
        this.paymentService = paymentService;
        this.expensesService = expensesService;
    }

    @Transactional(readOnly = true)
    public Optional<DeliveryRetrieveResponse> retrieve(Integer deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .map(this::toResponse);
    }

    @Transactional
    public DeliveryRetrieveResponse createDelivery(DeliveryRetrieveResponse request) {
        Delivery delivery = new Delivery();
        if (request.getId() != null) {
            delivery.setId(request.getId());
        }
        if (request.getTrip() != null && request.getTrip().getTripId() != null) {
            Integer tripId = request.getTrip().getTripId();
            if (tripService.retrieve(tripId).isEmpty()) {
                throw new IllegalArgumentException("Trip not found: " + tripId);
            }
            delivery.setTripId(tripId);
        }
        Integer supplierPickupId = getEntityId(request.getSupplierPickup());
        if (supplierPickupId != null) {
            delivery.setSupplierPickupId(supplierPickupId);
        }

        Integer mortalityId = getEntityId(request.getMortality());
        if (mortalityId != null) {
            delivery.setMortalityId(mortalityId);
        }

        Integer paymentId = getEntityId(request.getPayment());
        if (paymentId != null) {
            delivery.setPaymentId(paymentId);
        }

        Integer expenseId = getEntityId(request.getExpenses());
        if (expenseId != null) {
            delivery.setExpenseId(expenseId);
        }
        delivery.setStatus(request.getStatus());
        delivery.setIsActive(request.getIsActive());
        delivery.setIsDeleted(request.getIsDeleted());
        delivery.setCreateDate(request.getCreateDate());
        delivery.setUpdatedDate(request.getUpdatedDate());
        delivery.setCreatedBy(request.getCreatedBy());
        delivery.setUpdatedBy(request.getUpdatedBy());
        Delivery savedDelivery = deliveryRepository.save(delivery);

        if (request.getVendorDeliveries() != null) {
            for (VendorDeliveryRetrieveResponse vendorRequest : request.getVendorDeliveries()) {
                saveVendorDelivery(savedDelivery, vendorRequest);
            }
        }

        return toResponse(savedDelivery);
    }

    private Integer getEntityId(Object entity) {
        if (entity == null) {
            return null;
        }
        try {
            return (Integer) entity.getClass().getMethod("getId").invoke(entity);
        } catch (Exception ex) {
            return null;
        }
    }

    private void saveVendorDelivery(
            Delivery delivery,
            VendorDeliveryRetrieveResponse request
    ) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("Vendor delivery id is required");
        }
        if (request.getRelationId() == null) {
            throw new IllegalArgumentException("Vendor delivery relation id is required");
        }

        VendorDelivery vendorDelivery = new VendorDelivery();
        vendorDelivery.setId(request.getId());
        vendorDelivery.setSellingPartyId(saveSellingParty(request.getSellingParty()));

        vendorDelivery.setNote(request.getNote());
        vendorDelivery.setIsActive(request.getIsActive());
        vendorDelivery.setIsDeleted(request.getIsDeleted());
        vendorDelivery.setCreateDate(request.getCreateDate());
        vendorDelivery.setUpdatedDate(request.getUpdatedDate());
        vendorDelivery.setCreatedBy(request.getCreatedBy());
        vendorDelivery.setUpdatedBy(request.getUpdatedBy());
        VendorDelivery savedVendorDelivery = vendorDeliveryService.create(vendorDelivery);

        partyOrderRelationService.replaceOrders(
                savedVendorDelivery.getId(),
                request.getActualOrders(),
                PartyOrderRelationService.VENDOR_DELIVERY_PARTY_TYPE);

        DeliveryPartyRelation relation = new DeliveryPartyRelation();
        relation.setId(request.getRelationId());
        relation.setDeliveryId(delivery.getId());
        relation.setPartyId(savedVendorDelivery.getId());
        relation.setPartyType(PartyOrderRelationService.VENDOR_DELIVERY_PARTY_TYPE);
        relation.setIsActive(true);
        relation.setIsDeleted(false);
        relation.setCreateDate(request.getCreateDate());
        relation.setCreatedBy(request.getCreatedBy());
        deliveryPartyRelationService.create(relation);
    }

    private Integer saveSellingParty(SellingParty partyRequest) {
        if (partyRequest == null) {
            return null;
        }
        if (partyRequest.getId() == null) {
            return sellingPartyService.create(partyRequest).getId();
        }
        return sellingPartyService.update(partyRequest.getId(), partyRequest)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Selling party not found: " + partyRequest.getId()))
                .getId();
    }

    private DeliveryRetrieveResponse toResponse(Delivery delivery) {
        List<VendorDeliveryRetrieveResponse> vendorDeliveries =
                deliveryPartyRelationService.findActiveByDeliveryId(delivery.getId()).stream()
                        .map(this::toVendorDeliveryResponse)
                        .toList();

        Trip trip = delivery.getTripId() == null
                ? null
                : tripService.findById(delivery.getTripId()).orElse(null);

        SupplierPickup supplierPickup = delivery.getSupplierPickupId() == null
                ? null
                : supplierPickupService.findById(delivery.getSupplierPickupId()).orElse(null);

        Mortality mortality = delivery.getMortalityId() == null
                ? null
                : mortalityService.findById(delivery.getMortalityId()).orElse(null);

        Payment payment = delivery.getPaymentId() == null
                ? null
                : paymentService.findById(delivery.getPaymentId()).orElse(null);

        Expenses expenses = delivery.getExpenseId() == null
                ? null
                : expensesService.findById(delivery.getExpenseId()).orElse(null);

        return DeliveryRetrieveResponse.builder()
                .id(delivery.getId())
                .trip(trip)
                .supplierPickup(supplierPickup)
                .mortality(mortality)
                .payment(payment)
                .expenses(expenses)
                .status(delivery.getStatus())
                .isActive(delivery.getIsActive())
                .isDeleted(delivery.getIsDeleted())
                .createDate(delivery.getCreateDate())
                .updatedDate(delivery.getUpdatedDate())
                .createdBy(delivery.getCreatedBy())
                .updatedBy(delivery.getUpdatedBy())
                .vendorDeliveries(vendorDeliveries)
                .build();
    }

    private VendorDeliveryRetrieveResponse toVendorDeliveryResponse(DeliveryPartyRelation relation) {
        VendorDelivery vendorDelivery = vendorDeliveryService.findById(relation.getPartyId())
                .orElseThrow(() -> new IllegalStateException(
                        "Vendor delivery not found for delivery-party relation " + relation.getId()));
        return VendorDeliveryRetrieveResponse.builder()
                .relationId(relation.getId())
                .id(vendorDelivery.getId())
                .sellingParty(vendorDelivery.getSellingPartyId() == null
                        ? null
                        : sellingPartyService.findById(vendorDelivery.getSellingPartyId()).orElse(null))
                .actualOrders(partyOrderRelationService.findActiveOrders(
                        vendorDelivery.getId(), PartyOrderRelationService.VENDOR_DELIVERY_PARTY_TYPE))
                .note(vendorDelivery.getNote())
                .isActive(vendorDelivery.getIsActive())
                .isDeleted(vendorDelivery.getIsDeleted())
                .createDate(vendorDelivery.getCreateDate())
                .updatedDate(vendorDelivery.getUpdatedDate())
                .createdBy(vendorDelivery.getCreatedBy())
                .updatedBy(vendorDelivery.getUpdatedBy())
                .build();
    }
}
