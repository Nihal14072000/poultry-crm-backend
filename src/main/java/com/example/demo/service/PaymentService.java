package com.example.demo.service;

import com.example.demo.entity.Payment;
import com.example.demo.repository.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentService extends CrudService<Payment> {
    public PaymentService(PaymentRepository repository) {
        super(repository, Payment::setId);
    }
}
