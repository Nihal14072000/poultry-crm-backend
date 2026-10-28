package com.example.demo.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class FinanceReceivablesRepository {
    private final JdbcClient jdbc;
    public FinanceReceivablesRepository(JdbcClient jdbc) { this.jdbc = jdbc; }

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
