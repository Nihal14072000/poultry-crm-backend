package com.example.demo.report;

import com.example.demo.security.TenantContext;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportDataService {
    private final JdbcClient jdbc;
    private final TripSummaryReportDataService tripSummaryData;

    public ReportDataService(JdbcClient jdbc, TripSummaryReportDataService tripSummaryData) {
        this.jdbc = jdbc;
        this.tripSummaryData = tripSummaryData;
    }

    public ReportData load(ReportJob job) {
        Map<String, Object> filters = job.filters() == null ? Map.of() : job.filters();
        Integer tripId = integerFilter(filters, "tripId");
        String tenantId = job.tenantId();
        if (ReportType.valueOf(job.reportType()) == ReportType.TRIP_SUMMARY) {
            return tripSummaryData.load(tripId);
        }
        Integer partyId = integerFilter(filters, "partyId");
        String paymentType = stringFilter(filters, "paymentType");
        String paymentStatus = stringFilter(filters, "status");
        List<Map<String, Object>> rows = switch (ReportType.valueOf(job.reportType())) {
            case SALES -> sales(job.fromDate(), job.toDate(), tripId, partyId, tenantId);
            case PURCHASE -> purchases(job.fromDate(), job.toDate(), tripId, partyId, tenantId);
            case PAYMENTS -> payments(job.fromDate(), job.toDate(), tripId, partyId, paymentType, paymentStatus, tenantId);
            case OUTSTANDING -> outstanding(job.toDate(), partyId, tenantId);
            case DELIVERIES -> deliveries(job.fromDate(), job.toDate(), tripId, partyId, tenantId);
            case TRIP_SUMMARY -> throw new IllegalStateException("Trip Summary dispatch should have returned earlier");
        };
        List<String> columns = rows.isEmpty() ? defaultColumns(job.reportType()) : List.copyOf(rows.get(0).keySet());
        BigDecimal amount = rows.stream().map(r -> decimal(r.get("amount") != null ? r.get("amount") : r.get("outstanding_amount")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal quantity = rows.stream().map(r -> decimal(r.get("quantity") != null ? r.get("quantity") : r.get("qty")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ReportData(job.reportType().substring(0, 1) + job.reportType().substring(1).toLowerCase() + " Report",
                columns, rows, rows.size(), amount, quantity);
    }

    private List<Map<String, Object>> sales(LocalDate from, LocalDate to, Integer tripId, Integer partyId, String tenantId) {
        return query("""
                select coalesce(vd.create_date::date, o.createdate::date) as report_date,
                       vd.id as delivery_id, sp.name as party_name, o.trip_id,
                       o.qty as quantity, o.weight, o.rate,
                       coalesce(o.total, o.weight * o.rate, 0) as amount
                  from demo.vendor_delievery vd
                  join demo.selling_party sp on sp.id = vd.selling_party_id and sp.tenant_id = :tenantId and sp.tenant_id = :tenantId
                  join demo.party_order_relation por on por.party_id = vd.id
                       and por.tenant_id = :tenantId and por.party_type = 'VENDOR_DELIVERY' and por.is_active is true and por.is_deleted is false
                  join demo."order" o on o.id = por.order_id and o.isactive is true and o.isdeleted is false
                 where vd.tenant_id = :tenantId and vd.isactive is true and vd.isdeleted is false
                   and coalesce(vd.create_date::date, o.createdate::date) between :fromDate and :toDate
                   and (cast(:tripId as integer) is null or o.trip_id = :tripId)
                   and (cast(:partyId as integer) is null or vd.selling_party_id = :partyId)
                   and exists (select 1 from demo.delievery_party_relation dpr
                               join demo.delievery d on d.id = dpr.delivery_id and d.tenant_id = :tenantId and d.isactive is true and d.isdeleted is false
                               where dpr.party_id = vd.id and dpr.tenant_id = :tenantId and dpr.party_type = 'VENDOR_DELIVERY'
                                 and dpr.isactive is true and dpr.isdeleted is false)
                 order by report_date, vd.id
                """, from, to, tripId, partyId, null, null, tenantId);
    }

    private List<Map<String, Object>> purchases(LocalDate from, LocalDate to, Integer tripId, Integer partyId, String tenantId) {
        return query("""
                select coalesce(sp.created_ate::date, o.createdate::date) as report_date,
                       sp.id as pickup_id, bp.name as party_name, o.trip_id,
                       o.qty as quantity, o.weight, o.rate,
                       coalesce(o.total, o.weight * o.rate, 0) as amount
                  from demo.supplier_pickup sp
                  join demo.buying_party bp on bp.id = sp.buying_party_id
                  join demo."order" o on o.id = sp.actual_order_id and o.isactive is true and o.isdeleted is false
                 where sp.tenant_id = :tenantId and sp.isactive is true and sp.isdeleted is false
                   and coalesce(sp.created_ate::date, o.createdate::date) between :fromDate and :toDate
                   and (cast(:tripId as integer) is null or o.trip_id = :tripId)
                   and (cast(:partyId as integer) is null or sp.buying_party_id = :partyId)
                 order by report_date, sp.id
                """, from, to, tripId, partyId, null, null, tenantId);
    }

    private List<Map<String, Object>> payments(LocalDate from, LocalDate to, Integer tripId, Integer partyId,
                                               String paymentType, String status, String tenantId) {
        return query("""
                select p.payment_date as report_date, p.id as payment_id, p.payment_type,
                       coalesce(bp.name, sp.name) as party_name, p.trip_id, p.payment_method,
                       p.status, p.external_reference, p.amount
                  from demo.payment p
                  left join demo.buying_party bp on bp.id = p.buying_party_id and bp.tenant_id = :tenantId
                  left join demo.selling_party sp on sp.id = p.selling_party_id and sp.tenant_id = :tenantId
                 where p.tenant_id = :tenantId and p.isactive is true and p.isdeleted is false
                   and p.payment_date between :fromDate and :toDate
                   and (cast(:tripId as integer) is null or p.trip_id = :tripId)
                   and (cast(:partyId as integer) is null or p.buying_party_id = :partyId or p.selling_party_id = :partyId)
                   and (cast(:paymentType as varchar) is null or p.payment_type = :paymentType)
                   and (cast(:status as varchar) is null or p.status = :status)
                 order by p.payment_date, p.id
                """, from, to, tripId, partyId, paymentType, status, tenantId);
    }

    private List<Map<String, Object>> outstanding(LocalDate asOf, Integer partyId, String tenantId) {
        return query("""
                select 'RECEIVABLE' as balance_type, s.id as settlement_id, s.trip_id,
                       bp.name as party_name, s.settlement_date as report_date,
                       s.buyer_due_amount as total_due, coalesce(paid.amount_paid, 0) as amount_paid,
                       (s.buyer_due_amount - coalesce(paid.amount_paid, 0)) as outstanding_amount
                  from demo.finance_order_settlement s
                  join demo.buying_party bp on bp.id = s.buying_party_id and bp.tenant_id = :tenantId
                  left join lateral (
                      select sum(a.allocated_amount) as amount_paid
                        from demo.finance_payment_allocation a
                        join demo.payment p on p.id = a.payment_id and p.tenant_id = :tenantId
                       where a.settlement_id = s.id and a.tenant_id = :tenantId and a.allocation_type = 'BUYER_RECEIPT'
                         and p.status = 'COMPLETED' and p.isactive is true and p.isdeleted is false
                         and p.payment_date <= :toDate
                  ) paid on true
                 where s.tenant_id = :tenantId and s.status <> 'VOID' and s.settlement_date <= :toDate
                   and (cast(:partyId as integer) is null or s.buying_party_id = :partyId)
                union all
                select 'PAYABLE' as balance_type, s.id as settlement_id, s.trip_id,
                       sp.name as party_name, s.settlement_date as report_date,
                       s.seller_due_amount as total_due, coalesce(paid.amount_paid, 0) as amount_paid,
                       (s.seller_due_amount - coalesce(paid.amount_paid, 0)) as outstanding_amount
                  from demo.finance_order_settlement s
                  join demo.selling_party sp on sp.id = s.selling_party_id and sp.tenant_id = :tenantId
                  left join lateral (
                      select sum(a.allocated_amount) as amount_paid
                        from demo.finance_payment_allocation a
                        join demo.payment p on p.id = a.payment_id
                       where a.settlement_id = s.id and a.allocation_type = 'SELLER_PAYOUT'
                         and p.status = 'COMPLETED' and p.isactive is true and p.isdeleted is false
                         and p.payment_date <= :toDate
                  ) paid on true
                 where s.status <> 'VOID' and s.settlement_date <= :toDate
                   and (cast(:partyId as integer) is null or s.selling_party_id = :partyId)
                 order by report_date, settlement_id
                """, LocalDate.of(1900, 1, 1), asOf, null, partyId, null, null, tenantId);
    }

    private List<Map<String, Object>> deliveries(LocalDate from, LocalDate to, Integer tripId, Integer partyId, String tenantId) {
        return query("""
                select d.createdate::date as report_date, d.id as delivery_id, d.status as delivery_status,
                       o.trip_id, sp.name as party_name, o.qty as quantity, o.weight,
                       coalesce(o.total, o.weight * o.rate, 0) as amount
                  from demo.delievery d
                  join demo.delievery_party_relation dpr on dpr.delivery_id = d.id and dpr.tenant_id = :tenantId
                       and dpr.party_type = 'VENDOR_DELIVERY' and dpr.isactive is true and dpr.isdeleted is false
                  join demo.vendor_delievery vd on vd.id = dpr.party_id and vd.tenant_id = :tenantId
                       and vd.isactive is true and vd.isdeleted is false
                  join demo.selling_party sp on sp.id = vd.selling_party_id
                  join demo.party_order_relation por on por.party_id = vd.id
                       and por.party_type = 'VENDOR_DELIVERY' and por.is_active is true and por.is_deleted is false
                  join demo."order" o on o.id = por.order_id and o.isactive is true and o.isdeleted is false
                 where d.tenant_id = :tenantId and d.isactive is true and d.isdeleted is false
                   and d.createdate::date between :fromDate and :toDate
                   and (cast(:tripId as integer) is null or o.trip_id = :tripId)
                   and (cast(:partyId as integer) is null or vd.selling_party_id = :partyId)
                 order by report_date, d.id
                """, from, to, tripId, partyId, null, null);
    }

    private List<Map<String, Object>> query(String sql, LocalDate from, LocalDate to, Integer tripId,
                                            Integer partyId, String paymentType, String status, String tenantId) {
        return jdbc.sql(sql).param("fromDate", from).param("toDate", to)
                .param("tripId", tripId).param("partyId", partyId)
                .param("paymentType", paymentType).param("status", status).param("tenantId", tenantId)
                .query((rs, row) -> {
                    var meta = rs.getMetaData();
                    Map<String, Object> result = new LinkedHashMap<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) result.put(meta.getColumnLabel(i), rs.getObject(i));
                    return result;
                }).list();
    }

    private Integer integerFilter(Map<String, Object> filters, String key) {
        Object value = filters.get(key);
        if (value == null || value.toString().isBlank()) return null;
        try { return Integer.valueOf(value.toString()); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("Filter " + key + " must be an integer"); }
    }

    private String stringFilter(Map<String, Object> filters, String key) {
        Object value = filters.get(key);
        return value == null || value.toString().isBlank() ? null : value.toString().trim().toUpperCase();
    }

    private BigDecimal decimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal b) return b;
        return new BigDecimal(value.toString());
    }

    private List<String> defaultColumns(String type) {
        return switch (type) {
            case "SALES", "PURCHASE" -> List.of("report_date", "party_name", "trip_id", "quantity", "weight", "rate", "amount");
            case "PAYMENTS" -> List.of("report_date", "payment_type", "party_name", "payment_method", "status", "amount");
            case "OUTSTANDING" -> List.of("balance_type", "settlement_id", "trip_id", "party_name", "total_due", "amount_paid", "outstanding_amount");
            default -> List.of("report_date", "delivery_id", "delivery_status", "trip_id", "party_name", "quantity", "amount");
        };
    }
}
