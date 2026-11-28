package com.example.demo.repository;

import com.example.demo.entity.Order;
import com.example.demo.entity.SupplierPickup;
import com.example.demo.entity.VendorDelivery;
import com.example.demo.entity.Delivery;
import com.example.demo.entity.DeliveryPartyRelation;
import com.example.demo.entity.Trip;
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
    void actualSupplierPickupQueryReturnsActualOrdersAndTripIdButNotProposedOrders() {
        SupplierPickup pickup = new SupplierPickup();
        pickup.setBuyingPartyId(25);
        pickup.setCreatedDate(LocalDateTime.parse("2026-11-20T10:00:00"));
        pickup.setIsActive(true);
        pickup.setIsDeleted(false);
        entityManager.persist(pickup);

        Integer tripId = persistTrip("Assigned");
        Delivery tripDelivery = new Delivery();
        tripDelivery.setTripId(tripId);
        tripDelivery.setSupplierPickupId(pickup.getId());
        tripDelivery.setIsActive(true);
        tripDelivery.setIsDeleted(false);
        entityManager.persist(tripDelivery);

        Order actual = persistOrder(null);
        actual.setOrderTypeId(1);
        actual.setQty(8);
        actual.setWeight(new BigDecimal("20.50"));
        actual.setRate(new BigDecimal("100.00"));
        entityManager.merge(actual);
        persistRelation(pickup.getId(), "SUPPLIER_PICKUP", actual.getId());

        Order proposed = persistOrder(tripId);
        persistRelation(25, "B", proposed.getId());
        entityManager.flush();
        entityManager.clear();

        var result = orderRepository.findActualSupplierPickupOrders(1L, 25);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(actual.getId());
        assertThat(result.get(0).tripId()).isEqualTo(tripId);
        assertThat(result.get(0).weight()).isEqualByComparingTo("20.50");
        assertThat(result.get(0).createDate()).isEqualTo(LocalDateTime.parse("2026-11-20T10:00:00"));
    }

    @Test
    void actualVendorDeliveryQueryReturnsActualOrdersAndTripIdButNotProposedOrders() {
        VendorDelivery vendorDelivery = new VendorDelivery();
        vendorDelivery.setSellingPartyId(35);
        vendorDelivery.setCreateDate(LocalDateTime.parse("2026-11-21T11:00:00"));
        vendorDelivery.setIsActive(true);
        vendorDelivery.setIsDeleted(false);
        entityManager.persist(vendorDelivery);

        Integer tripId = persistTrip("In transit");
        Delivery tripDelivery = new Delivery();
        tripDelivery.setTripId(tripId);
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

        Order actual = persistOrder(null);
        actual.setOrderTypeId(1);
        actual.setQty(12);
        actual.setWeight(new BigDecimal("30.00"));
        actual.setRate(new BigDecimal("110.00"));
        entityManager.merge(actual);
        persistRelation(vendorDelivery.getId(), "VENDOR_DELIVERY", actual.getId());

        Order proposed = persistOrder(tripId);
        persistRelation(35, "S", proposed.getId());
        entityManager.flush();
        entityManager.clear();

        var result = orderRepository.findActualVendorDeliveryOrders(1L, 35);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(actual.getId());
        assertThat(result.get(0).tripId()).isEqualTo(tripId);
        assertThat(result.get(0).weight()).isEqualByComparingTo("30.00");
        assertThat(result.get(0).createDate()).isEqualTo(LocalDateTime.parse("2026-11-21T11:00:00"));
    }

    @Test
    void actualPartyOrdersExcludeMissingAndInactiveTrips() {
        SupplierPickup pickupWithoutTrip = new SupplierPickup();
        pickupWithoutTrip.setBuyingPartyId(25);
        pickupWithoutTrip.setCreatedDate(LocalDateTime.parse("2026-11-22T10:00:00"));
        pickupWithoutTrip.setIsActive(true);
        pickupWithoutTrip.setIsDeleted(false);
        entityManager.persist(pickupWithoutTrip);

        SupplierPickup pickupWithInactiveTrip = new SupplierPickup();
        pickupWithInactiveTrip.setBuyingPartyId(25);
        pickupWithInactiveTrip.setCreatedDate(LocalDateTime.parse("2026-11-22T11:00:00"));
        pickupWithInactiveTrip.setIsActive(true);
        pickupWithInactiveTrip.setIsDeleted(false);
        entityManager.persist(pickupWithInactiveTrip);

        Delivery inactivePickupTrip = new Delivery();
        inactivePickupTrip.setTripId(73);
        inactivePickupTrip.setSupplierPickupId(pickupWithInactiveTrip.getId());
        inactivePickupTrip.setIsActive(false);
        inactivePickupTrip.setIsDeleted(false);
        entityManager.persist(inactivePickupTrip);

        Order pickupWithoutTripOrder = persistOrder(null);
        Order pickupInactiveTripOrder = persistOrder(null);
        persistRelation(pickupWithoutTrip.getId(), "SUPPLIER_PICKUP", pickupWithoutTripOrder.getId());
        persistRelation(pickupWithInactiveTrip.getId(), "SUPPLIER_PICKUP", pickupInactiveTripOrder.getId());

        VendorDelivery vendorWithoutTrip = new VendorDelivery();
        vendorWithoutTrip.setSellingPartyId(35);
        vendorWithoutTrip.setCreateDate(LocalDateTime.parse("2026-11-22T12:00:00"));
        vendorWithoutTrip.setIsActive(true);
        vendorWithoutTrip.setIsDeleted(false);
        entityManager.persist(vendorWithoutTrip);

        VendorDelivery vendorWithInactiveTrip = new VendorDelivery();
        vendorWithInactiveTrip.setSellingPartyId(35);
        vendorWithInactiveTrip.setCreateDate(LocalDateTime.parse("2026-11-22T13:00:00"));
        vendorWithInactiveTrip.setIsActive(true);
        vendorWithInactiveTrip.setIsDeleted(false);
        entityManager.persist(vendorWithInactiveTrip);

        Delivery inactiveVendorTrip = new Delivery();
        inactiveVendorTrip.setTripId(74);
        inactiveVendorTrip.setVendorDelieveryId(vendorWithInactiveTrip.getId());
        inactiveVendorTrip.setIsActive(false);
        inactiveVendorTrip.setIsDeleted(false);
        entityManager.persist(inactiveVendorTrip);

        Order vendorWithoutTripOrder = persistOrder(null);
        Order vendorInactiveTripOrder = persistOrder(null);
        persistRelation(vendorWithoutTrip.getId(), "VENDOR_DELIVERY", vendorWithoutTripOrder.getId());
        persistRelation(vendorWithInactiveTrip.getId(), "VENDOR_DELIVERY", vendorInactiveTripOrder.getId());

        entityManager.flush();
        entityManager.clear();

        assertThat(orderRepository.findActualSupplierPickupOrders(1L, 25)).isEmpty();
        assertThat(orderRepository.findActualVendorDeliveryOrders(1L, 35)).isEmpty();
    }

    @Test
    void actualPartyOrdersExcludeCompletedAndDeletedTripsButIncludeActiveTripStatuses() {
        SupplierPickup pickup = new SupplierPickup();
        pickup.setBuyingPartyId(25);
        pickup.setCreatedDate(LocalDateTime.parse("2026-11-23T10:00:00"));
        pickup.setIsActive(true);
        pickup.setIsDeleted(false);
        entityManager.persist(pickup);

        Integer assignedTripId = persistTrip("Assigned");
        Integer completedTripId = persistTrip("Completed");
        Integer deletedTripId = persistTrip("Deleted");

        Delivery assignedDelivery = new Delivery();
        assignedDelivery.setTripId(assignedTripId);
        assignedDelivery.setSupplierPickupId(pickup.getId());
        assignedDelivery.setIsActive(true);
        assignedDelivery.setIsDeleted(false);
        entityManager.persist(assignedDelivery);

        SupplierPickup completedPickup = new SupplierPickup();
        completedPickup.setBuyingPartyId(25);
        completedPickup.setCreatedDate(LocalDateTime.parse("2026-11-23T11:00:00"));
        completedPickup.setIsActive(true);
        completedPickup.setIsDeleted(false);
        entityManager.persist(completedPickup);
        Delivery completedDelivery = new Delivery();
        completedDelivery.setTripId(completedTripId);
        completedDelivery.setSupplierPickupId(completedPickup.getId());
        completedDelivery.setIsActive(true);
        completedDelivery.setIsDeleted(false);
        entityManager.persist(completedDelivery);

        SupplierPickup deletedPickup = new SupplierPickup();
        deletedPickup.setBuyingPartyId(25);
        deletedPickup.setCreatedDate(LocalDateTime.parse("2026-11-23T12:00:00"));
        deletedPickup.setIsActive(true);
        deletedPickup.setIsDeleted(false);
        entityManager.persist(deletedPickup);
        Delivery deletedDelivery = new Delivery();
        deletedDelivery.setTripId(deletedTripId);
        deletedDelivery.setSupplierPickupId(deletedPickup.getId());
        deletedDelivery.setIsActive(true);
        deletedDelivery.setIsDeleted(false);
        entityManager.persist(deletedDelivery);

        Order activeOrder = persistOrder(null);
        Order completedOrder = persistOrder(null);
        Order deletedOrder = persistOrder(null);
        persistRelation(pickup.getId(), "SUPPLIER_PICKUP", activeOrder.getId());
        persistRelation(completedPickup.getId(), "SUPPLIER_PICKUP", completedOrder.getId());
        persistRelation(deletedPickup.getId(), "SUPPLIER_PICKUP", deletedOrder.getId());

        entityManager.flush();
        entityManager.clear();

        assertThat(orderRepository.findActualSupplierPickupOrders(1L, 25))
                .extracting(com.example.demo.dto.PartyActualOrderDto::id)
                .containsExactly(activeOrder.getId());
    }

    private Integer persistTrip(String status) {
        Trip trip = new Trip();
        trip.setStatus(status);
        entityManager.persist(trip);
        entityManager.flush();
        return trip.getTripId();
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
