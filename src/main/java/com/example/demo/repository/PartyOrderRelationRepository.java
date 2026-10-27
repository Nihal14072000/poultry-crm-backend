package com.example.demo.repository;

import com.example.demo.entity.PartyOrderRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyOrderRelationRepository extends JpaRepository<PartyOrderRelation, Integer> {
    List<PartyOrderRelation> findByPartyIdAndPartyType(Integer partyId, String partyType);
}
