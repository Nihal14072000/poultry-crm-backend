package com.example.demo.service;

import com.example.demo.entity.Expenses;
import com.example.demo.repository.ExpensesRepository;
import org.springframework.stereotype.Service;

@Service
public class ExpensesService extends CrudService<Expenses> {
    public ExpensesService(ExpensesRepository repository) {
        super(repository, Expenses::setId);
    }
}
