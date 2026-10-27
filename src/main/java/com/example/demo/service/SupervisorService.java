package com.example.demo.service;

import com.example.demo.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

@Service
public class SupervisorService extends TypedEmployeeService {
    private static final String SUPERVISOR_TYPE = "SUPERVISOR";

    public SupervisorService(EmployeeRepository repository, EmployeeAccountProvisioner accountProvisioner) {
        super(repository, SUPERVISOR_TYPE, accountProvisioner);
    }
}
