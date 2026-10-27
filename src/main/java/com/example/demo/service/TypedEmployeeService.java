package com.example.demo.service;

import com.example.demo.entity.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public abstract class TypedEmployeeService extends CrudService<Employee> {
    private final EmployeeRepository repository;
    private final String employeeType;

    protected TypedEmployeeService(EmployeeRepository repository, String employeeType) {
        super(repository, Employee::setId);
        this.repository = repository;
        this.employeeType = employeeType;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return repository.findAllByType(employeeType);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> findById(Integer id) {
        return repository.findByIdAndType(id, employeeType);
    }

    @Override
    @Transactional
    public Employee create(Employee employee) {
        employee.setType(employeeType);
        return repository.save(employee);
    }

    @Override
    @Transactional
    public Optional<Employee> update(Integer id, Employee employee) {
        if (!repository.existsByIdAndType(id, employeeType)) {
            return Optional.empty();
        }
        employee.setId(id);
        employee.setType(employeeType);
        return Optional.of(repository.save(employee));
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        if (!repository.existsByIdAndType(id, employeeType)) {
            return false;
        }
        repository.deleteByIdAndType(id, employeeType);
        return true;
    }
}
