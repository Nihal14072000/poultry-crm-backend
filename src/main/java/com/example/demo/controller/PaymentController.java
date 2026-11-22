package com.example.demo.controller;

import com.example.demo.dto.PaymentRetrieveResponse;
import com.example.demo.dto.PaymentSumResponse;
import com.example.demo.dto.PaymentWriteRequest;
import com.example.demo.entity.Payment;
import com.example.demo.service.PaymentService;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.LocalDate;

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
    public ResponseEntity<List<PaymentRetrieveResponse>> retrieveAll(
            @RequestParam(required = false) Integer partyId,
            @RequestParam(required = false) String partyTyp
    ) {
        if (partyId == null && partyTyp == null) {
            return ResponseEntity.ok(service.retrieveAll());
        }
        if (partyId == null || partyId <= 0
                || !("B".equals(partyTyp) || "S".equals(partyTyp))) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.retrieveByParty(partyId, partyTyp));
    }

    @GetMapping("/party/sum")
    public PaymentSumResponse sumCompletedPaymentsByParty(
            @RequestParam Integer partyId,
            @RequestParam String partyType,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        return service.sumCompletedPaymentsByParty(partyId, partyType, startDate, endDate);
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
