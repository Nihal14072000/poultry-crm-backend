package com.example.demo.service;

import com.example.demo.entity.DeliveryPartyRelation;
import com.example.demo.repository.DeliveryPartyRelationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliveryPartyRelationService extends CrudService<DeliveryPartyRelation> {
    private final DeliveryPartyRelationRepository repository;

    public DeliveryPartyRelationService(DeliveryPartyRelationRepository repository) {
        super(repository, DeliveryPartyRelation::setId);
        this.repository = repository;
    }

    public List<DeliveryPartyRelation> findByDeliveryId(Integer deliveryId) {
        return repository.findByDeliveryId(deliveryId);
    }

    public List<DeliveryPartyRelation> findActiveByDeliveryId(Integer deliveryId) {
        return repository.findByDeliveryId(deliveryId).stream()
                .filter(relation -> !Boolean.TRUE.equals(relation.getIsDeleted())
                        && !Boolean.FALSE.equals(relation.getIsActive()))
                .toList();
    }
}
