package com.example.demo.service;

import com.example.demo.entity.VendorDelivery;
import com.example.demo.repository.VendorDeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class VendorDeliveryService extends CrudService<VendorDelivery> {
    public VendorDeliveryService(VendorDeliveryRepository repository) {
        super(repository, VendorDelivery::setId);
    }

    @Override
    @Transactional
    public Optional<VendorDelivery> update(Integer id, VendorDelivery delivery) {
        Optional<VendorDelivery> existing = super.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        VendorDelivery current = existing.get();
        delivery.setIsActive(current.getIsActive());
        delivery.setIsDeleted(current.getIsDeleted());
        return super.update(id, delivery);
    }
}
