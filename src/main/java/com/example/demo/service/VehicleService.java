package com.example.demo.service;

import com.example.demo.entity.Vehicle;
import com.example.demo.repository.VehicleRepository;
import org.springframework.stereotype.Service;

@Service
public class VehicleService extends CrudService<Vehicle> {
    public VehicleService(VehicleRepository repository) {
        super(repository, Vehicle::setId);
    }
}
