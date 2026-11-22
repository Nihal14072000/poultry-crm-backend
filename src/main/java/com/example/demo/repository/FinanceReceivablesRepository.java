package com.example.demo.repository;

import com.example.demo.dto.FinanceSummaryResponse;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class FinanceReceivablesRepository {
    private final JdbcClient jdbc;
    public FinanceReceivablesRepository(JdbcClient jdbc) { this.jdbc = jdbc; }

    public FinanceSummaryResponse summary(Integer buyingPartyId, Integer sellingPartyId) {
        return jdbc.sql("""
                select
                    coalesce((select sum(p.amount)
                              from demo.payment p
                              where p.payment_type = 'BUYER_RECEIPT'
                                and p.status = 'COMPLETED'
                                and p.isactive is true
                                and p.isdeleted is not true
                                and (cast(:sellingPartyId as integer) is null or p.selling_party_id = :sellingPartyId)), 0) as total_received,
                    coalesce((select sum(coalesce(o.weight, 0) * coalesce(o.rate, 0))
                              from demo."order" o
                              where o.isactive is true
                                and o.isdeleted is not true
                                and exists (
                                    select 1
                                    from demo.party_order_relation por
                                    where por.order_id = o.id
                                      and por.party_type = 'B'
                                      and por.is_active is true
                                      and por.is_deleted is false
                                      and (cast(:buyingPartyId as integer) is null or por.party_id = :buyingPartyId)
                                )), 0) as total_receivable,
                    coalesce((select sum(p.amount)
                              from demo.payment p
                              where p.payment_type = 'SELLER_PAYOUT'
                                and p.status = 'COMPLETED'
                                and p.isactive is true
                                and p.isdeleted is not true
                                and (cast(:buyingPartyId as integer) is null or p.buying_party_id = :buyingPartyId)), 0) as total_paid,
                    coalesce((select sum(coalesce(o.total, coalesce(o.weight, 0) * coalesce(o.rate, 0)))
                              from demo."order" o
                              join demo.party_order_relation por
                                on por.order_id = o.id
                               and por.party_type = 'VENDOR_DELIVERY'
                               and por.is_active is true
                               and por.is_deleted is false
                              join demo.vendor_delievery vd
                                on vd.id = por.party_id
                               and vd.isactive is true
                               and vd.isdeleted is false
                              where o.isactive is true
                                and o.isdeleted is not true
                                and (cast(:sellingPartyId as integer) is null or vd.selling_party_id = :sellingPartyId)
                                and exists (
                                    select 1
                                    from demo.delievery_party_relation dpr
                                    join demo.delievery d
                                      on d.id = dpr.delivery_id
                                     and d.isactive is true
                                     and d.isdeleted is false
                                    where dpr.party_id = vd.id
                                      and dpr.party_type = 'VENDOR_DELIVERY'
                                      and dpr.isactive is true
                                      and dpr.isdeleted is false
                                )), 0) as total_payable
                """)
                .param("buyingPartyId", buyingPartyId)
                .param("sellingPartyId", sellingPartyId)
                .query((rs, row) -> new FinanceSummaryResponse(
                        rs.getBigDecimal("total_received"),
                        rs.getBigDecimal("total_receivable"),
                        rs.getBigDecimal("total_paid"),
                        rs.getBigDecimal("total_payable")
                ))
                .single();
    }

    public List<Map<String, Object>> buyers(Integer tripId, Integer partyId) {
        var query = jdbc.sql("select * from demo.finance_buyer_receivables_v where (cast(:tripId as integer) is null or trip_id = :tripId) and (cast(:partyId as integer) is null or buying_party_id = :partyId) order by trip_id, settlement_id")
                .param("tripId", tripId).param("partyId", partyId);
        return query.query((rs, row) -> {
            var metadata = rs.getMetaData(); Map<String, Object> result = new java.util.LinkedHashMap<>();
            for (int i = 1; i <= metadata.getColumnCount(); i++) result.put(metadata.getColumnLabel(i), rs.getObject(i));
            return result;
        }).list();
    }
    public List<Map<String, Object>> sellers(Integer tripId, Integer partyId) {
        var query = jdbc.sql("select * from demo.finance_seller_payables_v where (cast(:tripId as integer) is null or trip_id = :tripId) and (cast(:partyId as integer) is null or selling_party_id = :partyId) order by trip_id, settlement_id")
                .param("tripId", tripId).param("partyId", partyId);
        return query.query((rs, row) -> {
            var metadata = rs.getMetaData(); Map<String, Object> result = new java.util.LinkedHashMap<>();
            for (int i = 1; i <= metadata.getColumnCount(); i++) result.put(metadata.getColumnLabel(i), rs.getObject(i));
            return result;
        }).list();
    }
    public List<Map<String, Object>> trips(Integer tripId) {
        var query = jdbc.sql("select * from demo.finance_trip_payment_summary_v where (cast(:tripId as integer) is null or trip_id = :tripId) order by trip_id").param("tripId", tripId);
        return query.query((rs, row) -> {
            var metadata = rs.getMetaData(); Map<String, Object> result = new java.util.LinkedHashMap<>();
            for (int i = 1; i <= metadata.getColumnCount(); i++) result.put(metadata.getColumnLabel(i), rs.getObject(i));
            return result;
        }).list();
    }
}
