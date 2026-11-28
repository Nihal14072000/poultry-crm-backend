package com.example.demo.repository;

import com.example.demo.entity.Order;
import com.example.demo.entity.SupplierPickup;
import com.example.demo.entity.VendorDelivery;
import com.example.demo.entity.Delivery;
import com.example.demo.entity.DeliveryPartyRelation;
import com.example.demo.entity.PartyOrderRelation;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:party-order-relations;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS demo",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=demo"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PartyOrderRelationRepositoryIntegrationTest {

    @Autowired
    private PartyOrderRelationRepository repository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void tripScopedLookupReturnsOnlyTheSelectedTripsProposedRelations() {
        Order firstTripOrder = persistOrder(7);
        Order secondTripOrder = persistOrder(8);
        Order legacyOrderWithoutTrip = persistOrder(null);

        PartyOrderRelation firstTripRelation = persistRelation(2, "B", firstTripOrder.getId());
        persistRelation(2, "B", secondTripOrder.getId());
        persistRelation(2, "B", legacyOrderWithoutTrip.getId());
        entityManager.flush();
        entityManager.clear();

        List<PartyOrderRelation> result =
                repository.findByPartyIdAndPartyTypeAndTripId(2, "B", 7);

        assertThat(result)
                .extracting(PartyOrderRelation::getRelationId)
                .containsExactly(firstTripRelation.getRelationId());
    }

    @Test
    void actualPickupAndDeliveryLookupsRemainScopedToTheirRecordIdsAndPartyTypes() {
        Order actualPickupOrder = persistOrder(null);
        Order actualDeliveryOrder = persistOrder(null);
        Order proposedOrder = persistOrder(7);

        PartyOrderRelation pickupRelation =
                persistRelation(25, "SUPPLIER_PICKUP", actualPickupOrder.getId());
        PartyOrderRelation deliveryRelation =
                persistRelation(35, "VENDOR_DELIVERY", actualDeliveryOrder.getId());
        persistRelation(25, "B", proposedOrder.getId());
        persistRelation(35, "S", proposedOrder.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(repository.findByPartyIdAndPartyType(25, "SUPPLIER_PICKUP"))
                .extracting(PartyOrderRelation::getRelationId)
                .containsExactly(pickupRelation.getRelationId());
        assertThat(repository.findByPartyIdAndPartyType(35, "VENDOR_DELIVERY"))
                .extracting(PartyOrderRelation::getRelationId)
                .containsExactly(deliveryRelation.getRelationId());
    }

    @Test
    void sumsActiveOrderWeightTimesRateForTheRequestedPartyAndType() {
        Order matchingOrder = persistOrder(null);
        matchingOrder.setWeight(new BigDecimal("12.50"));
        matchingOrder.setRate(new BigDecimal("4.00"));
        entityManager.merge(matchingOrder);

        Order otherPartyOrder = persistOrder(null);
        otherPartyOrder.setWeight(new BigDecimal("100.00"));
        otherPartyOrder.setRate(new BigDecimal("10.00"));
        entityManager.merge(otherPartyOrder);

        persistRelation(25, "SUPPLIER_PICKUP", matchingOrder.getId());
        persistRelation(26, "SUPPLIER_PICKUP", otherPartyOrder.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(orderRepository.sumActiveOrderWeightTimesRateByPartyIdAndPartyType(
                1L, 25, "SUPPLIER_PICKUP"
        )).isEqualByComparingTo("50.00");
        assertThat(orderRepository.sumActiveOrderWeightTimesRateByPartyIdAndPartyType(
                1L, 99, "SUPPLIER_PICKUP"
        )).isEqualByComparingTo("0");
    }


    @Test
    void actualSupplierPickupQueryReturnsActualOrdersAndOrderTripIdButNotProposedOrders() {
        SupplierPickup pickup = new SupplierPickup();
        pickup.setBuyingPartyId(25);
        pickup.setCreatedDate(LocalDateTime.parse("2026-11-20T10:00:00"));
        pickup.setIsActive(true);
        pickup.setIsDeleted(false);
        entityManager.persist(pickup);

        Delivery tripDelivery = new Delivery();
        tripDelivery.setTripId(71);
        tripDelivery.setSupplierPickupId(pickup.getId());
        tripDelivery.setIsActive(true);
        tripDelivery.setIsDeleted(false);
        entityManager.persist(tripDelivery);

        Order actual = persistOrder(71);
        actual.setOrderTypeId(1);
        actual.setQty(8);
        actual.setWeight(new BigDecimal("20.50"));
        actual.setRate(new BigDecimal("100.00"));
        entityManager.merge(actual);
        persistRelation(pickup.getId(), "SUPPLIER_PICKUP", actual.getId());

        Order proposed = persistOrder(71);
        persistRelation(25, "B", proposed.getId());
        entityManager.flush();
        entityManager.clear();

        var result = orderRepository.findActualSupplierPickupOrders(1L, 25);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(actual.getId());
        assertThat(result.get(0).tripId()).isEqualTo(71);
        assertThat(result.get(0).weight()).isEqualByComparingTo("20.50");
        assertThat(result.get(0).createDate()).isEqualTo(LocalDateTime.parse("2026-11-20T10:00:00"));
    }

    @Test
    void actualVendorDeliveryQueryReturnsActualOrdersAndOrderTripIdButNotProposedOrders() {
        VendorDelivery vendorDelivery = new VendorDelivery();
        vendorDelivery.setSellingPartyId(35);
        vendorDelivery.setCreateDate(LocalDateTime.parse("2026-11-21T11:00:00"));
        vendorDelivery.setIsActive(true);
        vendorDelivery.setIsDeleted(false);
        entityManager.persist(vendorDelivery);

        Delivery tripDelivery = new Delivery();
        tripDelivery.setTripId(72);
        tripDelivery.setVendorDelieveryId(vendorDelivery.getId());
        tripDelivery.setIsActive(true);
        tripDelivery.setIsDeleted(false);
        entityManager.persist(tripDelivery);

        DeliveryPartyRelation deliveryRelation = new DeliveryPartyRelation();
        deliveryRelation.setId(900);
        deliveryRelation.setDeliveryId(tripDelivery.getId());
        deliveryRelation.setPartyId(vendorDelivery.getId());
        deliveryRelation.setPartyType("VENDOR_DELIVERY");
        deliveryRelation.setIsActive(true);
        deliveryRelation.setIsDeleted(false);
        entityManager.persist(deliveryRelation);

        Order actual = persistOrder(72);
        actual.setOrderTypeId(1);
        actual.setQty(12);
        actual.setWeight(new BigDecimal("30.00"));
        actual.setRate(new BigDecimal("110.00"));
        entityManager.merge(actual);
        persistRelation(vendorDelivery.getId(), "VENDOR_DELIVERY", actual.getId());

        Order proposed = persistOrder(72);
        persistRelation(35, "S", proposed.getId());
        entityManager.flush();
        entityManager.clear();

        var result = orderRepository.findActualVendorDeliveryOrders(1L, 35);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(actual.getId());
        assertThat(result.get(0).tripId()).isEqualTo(72);
        assertThat(result.get(0).weight()).isEqualByComparingTo("30.00");
        assertThat(result.get(0).createDate()).isEqualTo(LocalDateTime.parse("2026-11-21T11:00:00"));
    }

    private Order persistOrder(Integer tripId) {
        Order order = new Order();
        order.setTripId(tripId);
        order.setIsActive(true);
        order.setIsDeleted(false);
        entityManager.persist(order);
        entityManager.flush();
        return order;
    }

    private PartyOrderRelation persistRelation(Integer partyId, String partyType, Integer orderId) {
        PartyOrderRelation relation = new PartyOrderRelation();
        relation.setPartyId(partyId);
        relation.setPartyType(partyType);
        relation.setOrderId(orderId);
        relation.setIsActive(true);
        relation.setIsDeleted(false);
        entityManager.persist(relation);
        return relation;
    }
}
