package com.example.demo.service;

import com.example.demo.entity.TripPartyRelation;
import com.example.demo.entity.ActiveRecord;
import com.example.demo.repository.TripPartyRelationRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Replaces only the relationship types explicitly supplied by the caller.
     * A null list means "leave unchanged"; an empty list means "clear all".
     */
    @Transactional
    public void replaceParties(Integer tripId, List<Integer> buyingPartyIds, List<Integer> sellingPartyIds) {
        if (buyingPartyIds == null && sellingPartyIds == null) {
            return;
        }
        List<TripPartyRelation> existingRelations = repository.findByTripId(tripId);
        existingRelations.stream()
                .filter(this::isActive)
                .filter(relation -> BUYING_PARTY_TYPE.equals(relation.getPartyType())
                        ? buyingPartyIds != null
                        : SELLING_PARTY_TYPE.equals(relation.getPartyType()) && sellingPartyIds != null)
                .forEach(this::deactivate);
        repository.saveAll(existingRelations);

        List<TripPartyRelation> newRelations = new ArrayList<>();
        addRelations(newRelations, tripId, buyingPartyIds, BUYING_PARTY_TYPE);
        addRelations(newRelations, tripId, sellingPartyIds, SELLING_PARTY_TYPE);
        if (!newRelations.isEmpty()) {
            repository.saveAll(newRelations);
        }
    }

    @Transactional(readOnly = true)
    public List<TripPartyRelation> findActiveByTripId(Integer tripId) {
        return repository.findByTripId(tripId, Sort.by(Sort.Order.desc("createDate").nullsLast())).stream()
                .filter(ActiveRecord::isActiveRecord)
                .toList();
    }

    private void addRelations(List<TripPartyRelation> relations, Integer tripId,
                              List<Integer> partyIds, String partyType) {
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
            AuditSupport.markCreated(relation);
            relations.add(relation);
        }
    }

    private void deactivate(TripPartyRelation relation) {
        relation.setIsActive(false);
        relation.setIsDeleted(true);
        AuditSupport.markUpdated(relation, relation);
    }

    private boolean isActive(TripPartyRelation relation) {
        return Boolean.TRUE.equals(relation.getIsActive())
                && Boolean.FALSE.equals(relation.getIsDeleted());
    }
}
