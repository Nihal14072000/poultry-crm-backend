package com.example.demo.repository;

import com.example.demo.entity.DeliveryPartyRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryPartyRelationRepository extends JpaRepository<DeliveryPartyRelation, Integer> {
    List<DeliveryPartyRelation> findByDeliveryId(Integer deliveryId);
}
