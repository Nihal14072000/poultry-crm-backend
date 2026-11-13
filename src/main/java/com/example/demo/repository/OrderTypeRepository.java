package com.example.demo.repository;

import com.example.demo.entity.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderTypeRepository extends JpaRepository<OrderType, Integer> {
}
