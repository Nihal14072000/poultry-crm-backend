package com.example.demo.service;

import com.example.demo.entity.DeliveryPartyRelation;
import com.example.demo.entity.ActiveRecord;
import com.example.demo.entity.CreatedRecord;
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
        return CreatedRecord.newestFirst(repository.findByDeliveryId(deliveryId).stream()
                .filter(ActiveRecord::isActiveRecord)
                .toList());
    }

    public List<DeliveryPartyRelation> findActiveByDeliveryId(Integer deliveryId) {
        return findByDeliveryId(deliveryId);
    }
}
