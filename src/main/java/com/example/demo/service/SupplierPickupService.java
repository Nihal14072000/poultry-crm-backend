package com.example.demo.service;

import com.example.demo.dto.SupplierPickupRetrieveResponse;
import com.example.demo.entity.BuyingParty;
import com.example.demo.entity.Order;
import com.example.demo.entity.SupplierPickup;
import com.example.demo.repository.SupplierPickupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SupplierPickupService extends CrudService<SupplierPickup> {
    private final BuyingPartyService buyingPartyService;
    private final OrderService orderService;
    private final PartyOrderRelationService partyOrderRelationService;

    public SupplierPickupService(
            SupplierPickupRepository repository,
            BuyingPartyService buyingPartyService,
            OrderService orderService,
            PartyOrderRelationService partyOrderRelationService
    ) {
        super(repository, SupplierPickup::setId);
        this.buyingPartyService = buyingPartyService;
        this.orderService = orderService;
        this.partyOrderRelationService = partyOrderRelationService;
    }

    @Transactional(readOnly = true)
    public Optional<SupplierPickupRetrieveResponse> retrieve(Integer pickupId) {
        return findById(pickupId).map(this::toResponse);
    }

    @Transactional
    public SupplierPickupRetrieveResponse createAndRetrieve(SupplierPickup pickup) {
        return toResponse(super.create(pickup));
    }

    private SupplierPickupRetrieveResponse toResponse(SupplierPickup pickup) {
        BuyingParty buyingParty = pickup.getBuyingPartyId() == null
                ? null
                : buyingPartyService.findById(pickup.getBuyingPartyId()).orElse(null);

        return SupplierPickupRetrieveResponse.builder()
                .id(pickup.getId())
                .buyingParty(buyingParty)
                .actualOrders(partyOrderRelationService.findActiveOrders(
                        pickup.getId(), PartyOrderRelationService.SUPPLIER_PICKUP_PARTY_TYPE))
                .note(pickup.getNote())
                .isActive(pickup.getIsActive())
                .isDeleted(pickup.getIsDeleted())
                .createDate(pickup.getCreatedDate())
                .updatedDate(pickup.getUpdatedDate())
                .createdBy(pickup.getCreatedBy())
                .updatedBy(pickup.getUpdatedBy())
                .build();
    }
}
