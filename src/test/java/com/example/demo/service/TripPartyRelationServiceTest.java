package com.example.demo.service;

import com.example.demo.entity.TripPartyRelation;
import com.example.demo.repository.TripPartyRelationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripPartyRelationServiceTest {

    @Test
    void replacingBuyingPartiesLeavesSellingRelationshipsUntouched() {
        TripPartyRelation buying = relation(7, 10, "B");
        TripPartyRelation selling = relation(7, 20, "S");
        TripPartyRelationRepository repository = mock(TripPartyRelationRepository.class);
        when(repository.findByTripId(7)).thenReturn(List.of(buying, selling));

        new TripPartyRelationService(repository).replaceParties(7, List.of(11), null);

        assertFalse(buying.getIsActive());
        assertTrue(selling.getIsActive());

        ArgumentCaptor<List<TripPartyRelation>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository, times(2)).saveAll(captor.capture());
        List<TripPartyRelation> saved = captor.getAllValues().get(0);
        assertEquals(1L, saved.stream().filter(r -> "B".equals(r.getPartyType())
                && Boolean.TRUE.equals(r.getIsActive())).count());
        assertEquals(1L, saved.stream().filter(r -> "S".equals(r.getPartyType())
                && Boolean.TRUE.equals(r.getIsActive())).count());
    }

    @Test
    void emptyBuyingPartyListClearsBuyingRelationshipsButPreservesSellingRelationships() {
        TripPartyRelation buying = relation(7, 10, "B");
        TripPartyRelation selling = relation(7, 20, "S");
        TripPartyRelationRepository repository = mock(TripPartyRelationRepository.class);
        when(repository.findByTripId(7)).thenReturn(List.of(buying, selling));

        new TripPartyRelationService(repository).replaceParties(7, List.of(), null);

        assertFalse(buying.getIsActive());
        assertTrue(selling.getIsActive());
        verify(repository).saveAll(List.of(buying, selling));
    }

    private TripPartyRelation relation(Integer tripId, Integer partyId, String type) {
        TripPartyRelation relation = new TripPartyRelation();
        relation.setTripId(tripId);
        relation.setPartyId(partyId);
        relation.setPartyType(type);
        relation.setIsActive(true);
        relation.setIsDeleted(false);
        return relation;
    }
}
