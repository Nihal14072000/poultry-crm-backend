package com.example.demo.repository;

import com.example.demo.entity.FinancePaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Collection;

public interface FinancePaymentAllocationRepository extends JpaRepository<FinancePaymentAllocation, Integer> {
    List<FinancePaymentAllocation> findByPaymentId(Integer paymentId);
    List<FinancePaymentAllocation> findByPaymentIdIn(Collection<Integer> paymentIds);

    @Modifying
    @Query("""
            delete from FinancePaymentAllocation a
            where a.tenantId = :tenantId
              and a.paymentId in (select p.id from Payment p where p.tripId = :tripId and p.tenantId = :tenantId)
            """)
    void deleteByPaymentTripId(@Param("tenantId") Long tenantId, @Param("tripId") Integer tripId);

    @Query("select coalesce(sum(a.allocatedAmount), 0) from FinancePaymentAllocation a where a.tenantId = :tenantId and a.paymentId = :paymentId")
    BigDecimal sumAllocatedForPayment(@Param("tenantId") Long tenantId, @Param("paymentId") Integer paymentId);

    @Query("select coalesce(sum(a.allocatedAmount), 0) from FinancePaymentAllocation a join Payment p on p.id = a.paymentId where a.tenantId = :tenantId and a.settlementId = :settlementId and a.allocationType = :type and p.status = 'COMPLETED'")
    BigDecimal sumCompletedForSettlement(@Param("tenantId") Long tenantId, @Param("settlementId") Integer settlementId, @Param("type") String type);
}
