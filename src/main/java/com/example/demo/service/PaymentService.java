package com.example.demo.service;

import com.example.demo.entity.Payment;
import com.example.demo.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class PaymentService extends CrudService<Payment> {
    private final PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        super(repository, Payment::setId);
        this.repository = repository;
    }

    @Override
    @Transactional
    public Payment create(Payment payment) {
        validate(payment);
        if (payment.getCreateDate() == null) payment.setCreateDate(java.time.LocalDateTime.now());
        if (payment.getStatus() == null) payment.setStatus("PENDING");
        return repository.save(payment);
    }

    @Override
    @Transactional
    public java.util.Optional<Payment> update(Integer id, Payment payment) {
        if (!repository.existsById(id)) return java.util.Optional.empty();
        validate(payment);
        payment.setId(id);
        payment.setUpdatedDate(java.time.LocalDateTime.now());
        return java.util.Optional.of(repository.save(payment));
    }

    private void validate(Payment payment) {
        if (payment.getPaymentType() == null) return; // Allows legacy payment records.
        boolean buyer = "BUYER_RECEIPT".equals(payment.getPaymentType());
        boolean seller = "SELLER_PAYOUT".equals(payment.getPaymentType());
        if (!buyer && !seller) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment type");
        if (payment.getAmount() == null || payment.getAmount().signum() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount must be positive");
        if (buyer && (payment.getBuyingPartyId() == null || payment.getSellingPartyId() != null)
                || seller && (payment.getSellingPartyId() == null || payment.getBuyingPartyId() != null))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment must reference only the party for its type");
        if (payment.getStatus() != null && !java.util.List.of("PENDING", "COMPLETED", "FAILED", "VOID").contains(payment.getStatus()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment status");
        if (payment.getPaymentMethod() != null && !java.util.List.of("BANK_TRANSFER", "CASH", "CHEQUE", "UPI", "CARD", "OTHER").contains(payment.getPaymentMethod()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment method");
    }
}
