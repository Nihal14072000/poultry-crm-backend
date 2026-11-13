package com.example.demo.service;

import com.example.demo.entity.VendorDelivery;
import com.example.demo.repository.VendorDeliveryRepository;
import org.springframework.stereotype.Service;

@Service
public class VendorDeliveryService extends CrudService<VendorDelivery> {
    public VendorDeliveryService(VendorDeliveryRepository repository) {
        super(repository, VendorDelivery::setId);
    }
}
