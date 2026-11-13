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
                with vendor_deliveries as (
                    select d.trip_id, vd.id as vendor_delivery_id
                    from demo.delievery d
                    join demo.vendor_delievery vd on vd.id = d.vendor_delievery_id
                    where d.isactive is true
                      and d.isdeleted is not true
                      and vd.isactive is true
                      and vd.isdeleted is not true
                    union
                    select d.trip_id, vd.id as vendor_delivery_id
                    from demo.delievery d
                    join demo.delievery_party_relation dpr on dpr.delivery_id = d.id
                    join demo.vendor_delievery vd on vd.id = dpr.party_id
                    where d.isactive is true
                      and d.isdeleted is not true
                      and dpr.isactive is true
                      and dpr.isdeleted is not true
                      and dpr.party_type = 'VENDOR_DELIVERY'
                      and vd.isactive is true
                      and vd.isdeleted is not true
                ),
                vendor_orders as (
                    select distinct vd.trip_id, vendor.selling_party_id, o.id as order_id, o.qty, o.rate
                    from vendor_deliveries vd
                    join demo.vendor_delievery vendor on vendor.id = vd.vendor_delivery_id
                    join demo.selling_party party on party.id = vendor.selling_party_id
                    join demo.party_order_relation por
                      on por.party_id = vendor.id
                     and por.party_type = 'VENDOR_DELIVERY'
                     and por.is_active is true
                     and por.is_deleted is not true
                    join demo."order" o on o.id = por.order_id
                    where party.isactive is true
                      and party.isdeleted is not true
                      and o.isactive is true
                      and o.isdeleted is not true
                ),
                supplier_orders as (
                    select distinct d.trip_id, pickup.buying_party_id, o.id as order_id, o.qty, o.rate
                    from demo.delievery d
                    join demo.supplier_pickup pickup on pickup.id = d.supplier_pickup_id
                    join demo.buying_party party on party.id = pickup.buying_party_id
                    join demo.party_order_relation por
                      on por.party_id = pickup.id
                     and por.party_type = 'SUPPLIER_PICKUP'
                     and por.is_active is true
                     and por.is_deleted is not true
                    join demo."order" o on o.id = por.order_id
                    where d.isactive is true
                      and d.isdeleted is not true
                      and pickup.isactive is true
                      and pickup.isdeleted is not true
                      and party.isactive is true
                      and party.isdeleted is not true
                      and o.isactive is true
                      and o.isdeleted is not true
                )
                select
                    coalesce((select sum(p.amount)
                              from demo.payment p
                              where p.payment_type = 'BUYER_RECEIPT'
                                and p.status = 'COMPLETED'
                                and (cast(:buyingPartyId as integer) is null or p.buying_party_id = :buyingPartyId)), 0) as total_received,
                    coalesce((select sum(coalesce(o.qty, 0)::numeric * coalesce(o.rate, 0))
                              from vendor_orders o
                              where cast(:sellingPartyId as integer) is null
                                 or o.selling_party_id = :sellingPartyId), 0) as total_receivable,
                    coalesce((select sum(p.amount)
                              from demo.payment p
                              where p.payment_type = 'SELLER_PAYOUT'
                                and p.status = 'COMPLETED'
                                and (cast(:sellingPartyId as integer) is null or p.selling_party_id = :sellingPartyId)), 0) as total_paid,
                    coalesce((select sum(coalesce(o.qty, 0)::numeric * coalesce(o.rate, 0))
                              from supplier_orders o
                              where cast(:buyingPartyId as integer) is null
                                 or o.buying_party_id = :buyingPartyId), 0) as total_payable
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
