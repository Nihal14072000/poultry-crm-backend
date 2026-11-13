package com.example.demo.service;

import com.example.demo.entity.OrderType;
import com.example.demo.repository.OrderTypeRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderTypeService extends CrudService<OrderType> {
    public OrderTypeService(OrderTypeRepository repository) {
        super(repository, OrderType::setId);
    }
}
