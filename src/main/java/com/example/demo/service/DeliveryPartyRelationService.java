package com.example.demo.service;

import com.example.demo.entity.DeliveryPartyRelation;
import com.example.demo.entity.ActiveRecord;
import com.example.demo.repository.DeliveryPartyRelationRepository;
import com.example.demo.security.TenantContext;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class DeliveryPartyRelationService extends CrudService<DeliveryPartyRelation> {
    private final DeliveryPartyRelationRepository repository;

    public DeliveryPartyRelationService(DeliveryPartyRelationRepository repository) {
        super(repository, DeliveryPartyRelation::setId);
        this.repository = repository;
    }

    @Override
    @Transactional
    public DeliveryPartyRelation create(DeliveryPartyRelation relation) {
        // Relation IDs are supplied by the delivery workflow and are not generated.
        relation.setTenantId(TenantContext.requireTenantId());
        AuditSupport.markCreated(relation);
        return repository.save(relation);
    }

    public List<DeliveryPartyRelation> findByDeliveryId(Integer deliveryId) {
        long tenantId = TenantContext.requireTenantId();
        return repository.findByDeliveryId(deliveryId, Sort.by(Sort.Order.desc("createDate").nullsLast())).stream()
                .filter(ActiveRecord::isActiveRecord)
                .filter(relation -> Objects.equals(relation.getTenantId(), tenantId))
                .toList();
    }

    public List<DeliveryPartyRelation> findActiveByDeliveryId(Integer deliveryId) {
        return findByDeliveryId(deliveryId);
    }
}
