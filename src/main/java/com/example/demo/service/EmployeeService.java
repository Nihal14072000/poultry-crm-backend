package com.example.demo.service;

import com.example.demo.entity.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService extends CrudService<Employee> {
    public EmployeeService(EmployeeRepository repository) {
        super(repository, Employee::setId);
    }
}
