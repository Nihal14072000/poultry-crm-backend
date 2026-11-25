package com.example.demo.service;

import com.example.demo.entity.Employee;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class EmployeeService extends CrudService<Employee> {
    private final EmployeeRepository employeeRepository;
    private final EmployeeAccountProvisioner accountProvisioner;

    public EmployeeService(EmployeeRepository repository, EmployeeAccountProvisioner accountProvisioner) {
        super(repository, Employee::setId);
        this.employeeRepository = repository;
        this.accountProvisioner = accountProvisioner;
    }

    @Override
    @Transactional
    public Employee create(Employee employee){
        String username = accountProvisioner.validateNameForAccount(employee.getName());
        employee.setTenantId(TenantContext.requireTenantId());
        employee.setName(employee.getName().trim());
        AuditSupport.markCreated(employee);
        Employee savedEmployee = employeeRepository.save(employee);
        accountProvisioner.createAccount(savedEmployee, username);
        return savedEmployee;
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        long tenantId = TenantContext.requireTenantId();
        if (employeeRepository.findById(id).filter(e -> Objects.equals(e.getTenantId(), tenantId)).isEmpty()) {
            return false;
        }
        accountProvisioner.deleteAccount(id);
        employeeRepository.deleteById(id);
        return true;
    }
}
