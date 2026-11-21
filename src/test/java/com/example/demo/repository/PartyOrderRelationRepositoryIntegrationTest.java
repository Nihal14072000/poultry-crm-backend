package com.example.demo.repository;

import com.example.demo.entity.Order;
import com.example.demo.entity.PartyOrderRelation;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

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
