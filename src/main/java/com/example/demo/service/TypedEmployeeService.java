package com.example.demo.service;

import com.example.demo.entity.Employee;
import com.example.demo.entity.ActiveRecord;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.security.TenantContext;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class TypedEmployeeService extends CrudService<Employee> {
    private final EmployeeRepository repository;
    private final String employeeType;
    private final EmployeeAccountProvisioner accountProvisioner;

    protected TypedEmployeeService(
            EmployeeRepository repository,
            String employeeType,
            EmployeeAccountProvisioner accountProvisioner
    ) {
        super(repository, Employee::setId);
        this.repository = repository;
        this.employeeType = employeeType;
        this.accountProvisioner = accountProvisioner;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        long tenantId = TenantContext.requireTenantId();
        return repository.findAllByType(employeeType, Sort.by(Sort.Order.desc("createDate").nullsLast())).stream()
                .filter(ActiveRecord::isActiveRecord)
                .filter(employee -> Objects.equals(employee.getTenantId(), tenantId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> findById(Integer id) {
        long tenantId = TenantContext.requireTenantId();
        return repository.findByIdAndType(id, employeeType)
                .filter(employee -> Objects.equals(employee.getTenantId(), tenantId))
                .filter(employee -> Boolean.TRUE.equals(employee.getIsActive())
                        && Boolean.FALSE.equals(employee.getIsDeleted()));
    }

    @Override
    @Transactional
    public Employee create(Employee employee){
        String username = accountProvisioner.validateNameForAccount(employee.getName());
        employee.setTenantId(TenantContext.requireTenantId());
        employee.setName(employee.getName().trim());
        employee.setType(employeeType);
        AuditSupport.markCreated(employee);
        Employee savedEmployee = repository.save(employee);
        accountProvisioner.createAccount(savedEmployee, username);
        return savedEmployee;
    }

    @Override
    @Transactional
    public Optional<Employee> update(Integer id, Employee employee) {
        long tenantId = TenantContext.requireTenantId();
        Optional<Employee> existing = repository.findByIdAndType(id, employeeType)
                .filter(item -> Objects.equals(item.getTenantId(), tenantId));
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        employee.setId(id);
        employee.setTenantId(tenantId);
        employee.setType(employeeType);
        AuditSupport.markUpdated(employee, existing.get());
        return Optional.of(repository.save(employee));
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        long tenantId = TenantContext.requireTenantId();
        Optional<Employee> existing = repository.findByIdAndType(id, employeeType)
                .filter(item -> Objects.equals(item.getTenantId(), tenantId));
        if (existing.isEmpty()) {
            return false;
        }
        accountProvisioner.deleteAccount(id);
        repository.deleteByIdAndType(id, employeeType);
        return true;
    }
}
