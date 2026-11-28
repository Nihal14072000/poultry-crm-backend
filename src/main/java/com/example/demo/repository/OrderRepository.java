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

    @Query("""
            select distinct new com.example.demo.dto.PartyActualOrderDto(
                o.id, o.tripId, o.orderTypeId, o.qty, o.weight, o.rate, o.total, pickup.createdDate
            )
            from Order o
            join PartyOrderRelation relation on relation.orderId = o.id
            join Trip trip on trip.tripId = o.tripId
                and trip.tenantId = :tenantId
                and coalesce(lower(trip.status), '') <> 'deleted'
            join SupplierPickup pickup on pickup.id = relation.partyId
            join Delivery delivery on delivery.supplierPickupId = pickup.id
                and delivery.tripId = o.tripId
                and delivery.tenantId = :tenantId
                and delivery.isActive = true
                and delivery.isDeleted = false
            where o.tenantId = :tenantId
              and o.isActive = true
              and o.isDeleted = false
              and relation.tenantId = :tenantId
              and relation.partyType = 'SUPPLIER_PICKUP'
              and relation.isActive = true
              and relation.isDeleted = false
              and pickup.tenantId = :tenantId
              and pickup.buyingPartyId = :partyId
              and pickup.isActive = true
              and pickup.isDeleted = false
            order by pickup.createdDate desc, o.id desc
            """)
    List<com.example.demo.dto.PartyActualOrderDto> findActualSupplierPickupOrders(
            @Param("tenantId") Long tenantId,
            @Param("partyId") Integer partyId
    );

    @Query("""
            select distinct new com.example.demo.dto.PartyActualOrderDto(
                o.id, delivery.tripId, o.orderTypeId, o.qty, o.weight, o.rate, o.total, vendorDelivery.createDate
            )
            from Order o
            join PartyOrderRelation relation on relation.orderId = o.id
            join Trip trip on trip.tripId = o.tripId
                and trip.tenantId = :tenantId
                and coalesce(lower(trip.status), '') <> 'deleted'
            join VendorDelivery vendorDelivery on vendorDelivery.id = relation.partyId
            left join DeliveryPartyRelation deliveryRelation on deliveryRelation.partyId = vendorDelivery.id
                and deliveryRelation.partyType = 'VENDOR_DELIVERY'
                and deliveryRelation.tenantId = :tenantId
                and deliveryRelation.isActive = true
                and deliveryRelation.isDeleted = false
            join Delivery delivery on (delivery.id = deliveryRelation.deliveryId
                or delivery.vendorDelieveryId = vendorDelivery.id)
                and delivery.tripId = o.tripId
                and delivery.tenantId = :tenantId
                and delivery.isActive = true
                and delivery.isDeleted = false
            where o.tenantId = :tenantId
              and o.isActive = true
              and o.isDeleted = false
              and relation.tenantId = :tenantId
              and relation.partyType = 'VENDOR_DELIVERY'
              and relation.isActive = true
              and relation.isDeleted = false
              and vendorDelivery.tenantId = :tenantId
              and vendorDelivery.sellingPartyId = :partyId
              and vendorDelivery.isActive = true
              and vendorDelivery.isDeleted = false
            order by vendorDelivery.createDate desc, o.id desc
            """)
    List<com.example.demo.dto.PartyActualOrderDto> findActualVendorDeliveryOrders(
            @Param("tenantId") Long tenantId,
            @Param("partyId") Integer partyId
    );

}
