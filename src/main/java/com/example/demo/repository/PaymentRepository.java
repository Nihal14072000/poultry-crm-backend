package com.example.demo.repository;

import com.example.demo.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByTripId(Integer tripId);

    @Query("""
            select p from Payment p
            where p.tripId = :tripId
              and p.isActive = true
              and p.isDeleted = false
            order by case when p.createDate is null then 1 else 0 end, p.createDate desc
            """)
    List<Payment> findActiveByTripIdOrderByCreateDateDesc(@Param("tripId") Integer tripId);
    List<Payment> findByBuyingPartyId(Integer buyingPartyId);
    List<Payment> findBySellingPartyId(Integer sellingPartyId);

    @Query("""
            select sum(p.amount)
            from Payment p
            where ((:partyType = 'B' and p.buyingPartyId = :partyId)
                or (:partyType = 'S' and p.sellingPartyId = :partyId))
              and (:startDate is null or p.paymentDate >= :startDate)
              and (:endDate is null or p.paymentDate <= :endDate)
              and p.status = 'COMPLETED'
              and p.isActive = true
              and p.isDeleted = false
            """)
    BigDecimal sumCompletedPaymentsByParty(
            @Param("partyId") Integer partyId,
            @Param("partyType") String partyType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Modifying
    @Query("delete from Payment p where p.tripId = :tripId")
    void deleteByTripId(@Param("tripId") Integer tripId);
}
