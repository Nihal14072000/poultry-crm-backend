package com.example.demo.repository;

import com.example.demo.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByTripId(Integer tripId);
    @Modifying
    @Query("delete from Payment p where p.tripId = :tripId")
    void deleteByTripId(@Param("tripId") Integer tripId);
}
