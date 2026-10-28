package com.example.demo.repository;

import com.example.demo.entity.FinancePaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

public interface FinancePaymentAllocationRepository extends JpaRepository<FinancePaymentAllocation, Integer> {
    @Query("select coalesce(sum(a.allocatedAmount), 0) from FinancePaymentAllocation a where a.paymentId = :paymentId")
    BigDecimal sumAllocatedForPayment(@Param("paymentId") Integer paymentId);

    @Query("select coalesce(sum(a.allocatedAmount), 0) from FinancePaymentAllocation a join Payment p on p.id = a.paymentId where a.settlementId = :settlementId and a.allocationType = :type and p.status = 'COMPLETED'")
    BigDecimal sumCompletedForSettlement(@Param("settlementId") Integer settlementId, @Param("type") String type);
}
