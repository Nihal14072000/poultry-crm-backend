package com.example.demo.controller;

import com.example.demo.dto.PaymentRetrieveResponse;
import com.example.demo.dto.PaymentWriteRequest;
import com.example.demo.entity.Payment;
import com.example.demo.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @GetMapping
    public List<Payment> findAll() {
        return service.findAll();
    }

    @GetMapping("/retrieve")
    public List<PaymentRetrieveResponse> retrieveAll() {
        return service.retrieveAll();
    }

    @GetMapping("/trip/{tripId}/retrieve")
    public List<PaymentRetrieveResponse> retrieveByTripId(@PathVariable Integer tripId) {
        return service.retrieveByTripId(tripId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> findById(@PathVariable Integer id) {
        return service.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/retrieve")
    public ResponseEntity<PaymentRetrieveResponse> retrieve(@PathVariable Integer id) {
        return service.retrieve(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Payment> create(@RequestBody PaymentWriteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(toEntity(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payment> update(@PathVariable Integer id, @RequestBody PaymentWriteRequest request) {
        return service.update(id, toEntity(request)).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private Payment toEntity(PaymentWriteRequest request) {
        Payment payment = new Payment();
        payment.setBuyingPartyId(request.getBuyingPartyId());
        payment.setActualOrderId(request.getActualOrderId());
        payment.setTripId(request.getTripId());
        payment.setNote(request.getNote());
        payment.setPaymentType(request.getPaymentType());
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setExternalReference(request.getExternalReference());
        payment.setSellingPartyId(request.getSellingPartyId());
        payment.setStatus(request.getStatus());
        return payment;
    }
}
