package com.example.demo.repository;

import com.example.demo.entity.PartyOrderRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PartyOrderRelationRepository extends JpaRepository<PartyOrderRelation, Integer> {
    List<PartyOrderRelation> findByPartyIdAndPartyType(Integer partyId, String partyType);

    @Query("""
            select relation
            from PartyOrderRelation relation
            where relation.partyId = :partyId
              and relation.partyType = :partyType
              and exists (
                  select orderRecord.id
                  from Order orderRecord
                  where orderRecord.id = relation.orderId
                    and orderRecord.tripId = :tripId
              )
            """)
    List<PartyOrderRelation> findByPartyIdAndPartyTypeAndTripId(
            @Param("partyId") Integer partyId,
            @Param("partyType") String partyType,
            @Param("tripId") Integer tripId
    );
}
