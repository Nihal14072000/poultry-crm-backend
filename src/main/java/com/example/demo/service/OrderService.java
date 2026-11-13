package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService extends CrudService<Order> {
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        super(repository, Order::setId);
        this.repository = repository;
    }

    public List<Order> findActiveByPartyIdAndPartyType(Integer partyId, String partyType) {
        return repository.findActiveByPartyIdAndPartyType(partyId, partyType);
    }
}
