package com.example.demo.repository;

import com.example.demo.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("""
            select o from Order o
            where o.isActive = true
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
            @Param("partyId") Integer partyId,
            @Param("partyType") String partyType
    );
}
