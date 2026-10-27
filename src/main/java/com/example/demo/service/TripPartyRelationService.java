package com.example.demo.service;

import com.example.demo.entity.TripPartyRelation;
import com.example.demo.repository.TripPartyRelationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TripPartyRelationService {
    private static final String BUYING_PARTY_TYPE = "B";
    private static final String SELLING_PARTY_TYPE = "S";

    private final TripPartyRelationRepository repository;

    public TripPartyRelationService(TripPartyRelationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void replaceParties(Integer tripId, List<Integer> buyingPartyIds, List<Integer> sellingPartyIds) {
        LocalDateTime now = LocalDateTime.now();
        List<TripPartyRelation> existingRelations = repository.findByTripId(tripId);
        existingRelations.stream()
                .filter(this::isActive)
                .forEach(relation -> deactivate(relation, now));
        repository.saveAll(existingRelations);

        List<TripPartyRelation> newRelations = new ArrayList<>();
        addRelations(newRelations, tripId, buyingPartyIds, BUYING_PARTY_TYPE, now);
        addRelations(newRelations, tripId, sellingPartyIds, SELLING_PARTY_TYPE, now);
        repository.saveAll(newRelations);
    }

    @Transactional(readOnly = true)
    public List<TripPartyRelation> findActiveByTripId(Integer tripId) {
        return repository.findByTripId(tripId).stream()
                .filter(this::isActive)
                .toList();
    }

    private void addRelations(
            List<TripPartyRelation> relations,
            Integer tripId,
            List<Integer> partyIds,
            String partyType,
            LocalDateTime now
    ) {
        if (partyIds == null) {
            return;
        }

        for (Integer partyId : partyIds) {
            TripPartyRelation relation = new TripPartyRelation();
            relation.setTripId(tripId);
            relation.setPartyId(partyId);
            relation.setPartyType(partyType);
            relation.setIsActive(true);
            relation.setIsDeleted(false);
            relation.setCreateDate(now);
            relations.add(relation);
        }
    }

    private void deactivate(TripPartyRelation relation, LocalDateTime now) {
        relation.setIsActive(false);
        relation.setIsDeleted(true);
        relation.setUpdatedDate(now);
    }

    private boolean isActive(TripPartyRelation relation) {
        return !Boolean.TRUE.equals(relation.getIsDeleted())
                && !Boolean.FALSE.equals(relation.getIsActive());
    }
}
