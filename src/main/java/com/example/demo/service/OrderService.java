package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderService extends CrudService<Order> {
    public OrderService(OrderRepository repository) {
        super(repository, Order::setId);
    }
}
