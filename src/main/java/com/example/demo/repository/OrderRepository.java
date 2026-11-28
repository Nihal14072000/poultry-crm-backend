package com.example.demo.repository;

import com.example.demo.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Modifying
    @Query("delete from Order o where o.tripId = :tripId")
    void deleteByTripId(@Param("tripId") Integer tripId);

    @Query("""
            select o from Order o
            where o.tenantId = :tenantId
              and o.isActive = true
              and o.isDeleted = false
              and (:startDate is null or o.createDate >= :startDate)
              and (:endDateExclusive is null or o.createDate < :endDateExclusive)
            order by case when o.createDate is null then 1 else 0 end, o.createDate desc
            """)
    List<Order> findActiveInDateRange(
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDateExclusive") LocalDateTime endDateExclusive
    );

    @Query("""
            select o from Order o
            where o.tenantId = :tenantId
              and o.isActive = true
              and o.isDeleted = false
              and exists (
                  select relation.relationId
                  from PartyOrderRelation relation
                  where relation.orderId = o.id
                    and relation.partyId = :partyId
                    and relation.partyType = :partyType
                    and relation.isActive = true
                    and relation.isDeleted = false
              )
            order by case when o.createDate is null then 1 else 0 end, o.createDate desc
            """)
    List<Order> findActiveByPartyIdAndPartyType(
            @Param("tenantId") Long tenantId,
            @Param("partyId") Integer partyId,
            @Param("partyType") String partyType
    );

    @Query("""
            select coalesce(sum(coalesce(o.weight, 0) * coalesce(o.rate, 0)), 0)
            from Order o
            where o.tenantId = :tenantId
              and o.isActive = true
              and o.isDeleted = false
              and exists (
                  select relation.relationId
                  from PartyOrderRelation relation
                  where relation.orderId = o.id
                    and relation.partyId = :partyId
                    and relation.partyType = :partyType
                    and relation.isActive = true
                    and relation.isDeleted = false
              )
            """)
    BigDecimal sumActiveOrderWeightTimesRateByPartyIdAndPartyType(
            @Param("tenantId") Long tenantId,
            @Param("partyId") Integer partyId,
            @Param("partyType") String partyType
    );

    @Query("""
            select o from Order o
            where o.tenantId = :tenantId
              and o.isActive = true
              and o.isDeleted = false
              and (:startDate is null or o.createDate >= :startDate)
              and (:endDateExclusive is null or o.createDate < :endDateExclusive)
              and exists (
                  select relation.relationId
                  from PartyOrderRelation relation
                  where relation.orderId = o.id
                    and relation.partyId = :partyId
                    and relation.partyType = :partyType
                    and relation.isActive = true
                    and relation.isDeleted = false
              )
            order by case when o.createDate is null then 1 else 0 end, o.createDate desc
            """)
    List<Order> findActiveByPartyIdAndPartyTypeInDateRange(
            @Param("tenantId") Long tenantId,
            @Param("partyId") Integer partyId,
            @Param("partyType") String partyType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDateExclusive") LocalDateTime endDateExclusive
    );

    @Query("""
            select o from Order o
            where o.tenantId = :tenantId
              and o.tripId = :tripId
              and o.isActive = true
              and o.isDeleted = false
              and exists (
                  select relation.relationId
                  from PartyOrderRelation relation
                  where relation.orderId = o.id
                    and relation.partyId = :partyId
                    and relation.partyType = :partyType
                    and relation.isActive = true
                    and relation.isDeleted = false
              )
            order by case when o.createDate is null then 1 else 0 end, o.createDate desc
            """)
    List<Order> findActiveByTripIdAndPartyIdAndPartyType(
            @Param("tenantId") Long tenantId,
            @Param("tripId") Integer tripId,
            @Param("partyId") Integer partyId,
            @Param("partyType") String partyType
    );
}
