package com.example.demo.repository;

import com.example.demo.entity.TripPartyRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripPartyRelationRepository extends JpaRepository<TripPartyRelation, Integer> {
    List<TripPartyRelation> findByTripId(Integer tripId);
}
