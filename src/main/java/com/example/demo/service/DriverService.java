package com.example.demo.service;

import com.example.demo.entity.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DriverService extends TypedEmployeeService {
    private static final String DRIVER_TYPE = "DRIVER";

    private final EmployeeRepository repository;

    public DriverService(EmployeeRepository repository) {
        super(repository, DRIVER_TYPE);
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Employee> findAssignedToVehicle(Integer vehicleId) {
        return repository.findDistinctAssignedToVehicle(vehicleId, DRIVER_TYPE);
    }
}
