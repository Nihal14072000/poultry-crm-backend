package com.example.demo.report;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TripSummaryReportDataService {
    private static final List<String> COLUMNS = List.of(
            "record_type", "report_date", "reference_id", "party_name", "description",
            "quantity", "weight", "rate", "amount", "status");

    private final JdbcClient jdbc;

    public TripSummaryReportDataService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public ReportData load(Integer tripId) {
        if (tripId == null || tripId <= 0) {
            throw new IllegalArgumentException("A valid tripId is required for Trip Summary");
        }

        List<Map<String, Object>> rows = jdbc.sql("""
                select record_type, report_date, reference_id, party_name, description,
                       quantity, weight, rate, amount, status
                from (
                    select 'TRIP'::varchar as record_type,
                           t."date" as report_date,
                           t.trip_id as reference_id,
                           null::varchar as party_name,
                           concat_ws(' | ',
                               'Route: ' || coalesce(t.route, '-'),
                               'Vehicle ID: ' || coalesce(t.vehicle_id::varchar, '-'),
                               'Driver ID: ' || coalesce(t.driver_id::varchar, '-'),
                               'Supervisor ID: ' || coalesce(t.supervisor_id::varchar, '-'),
                               'Labour ID: ' || coalesce(t.labour_id::varchar, '-'),
                               'Pickup ID: ' || coalesce(t.pickup_id::varchar, '-'),
                               'Drop ID: ' || coalesce(t.drop_id::varchar, '-')) as description,
                           null::numeric as quantity,
                           null::numeric as weight,
                           null::numeric as rate,
                           null::numeric as amount,
                           t.status::varchar as status
                      from demo.trip t
                     where t.trip_id = :tripId

                    union all

                    select 'ORDER'::varchar,
                           coalesce(o.createdate::date, t."date"),
                           o.id,
                           coalesce(bp.name, sp.name),
                           'Trip order',
                           o.qty::numeric,
                           o.weight::numeric,
                           o.rate::numeric,
                           coalesce(o.total, o.weight * o.rate, 0)::numeric,
                           null::varchar
                      from demo."order" o
                      join demo.trip t on t.trip_id = o.trip_id
                      join demo.party_order_relation por on por.order_id = o.id
                           and por.party_type in ('B', 'S')
                           and por.is_active is true and por.is_deleted is false
                      left join demo.buying_party bp on bp.id = por.party_id and por.party_type = 'B'
                      left join demo.selling_party sp on sp.id = por.party_id and por.party_type = 'S'
                     where o.trip_id = :tripId
                       and o.isactive is true and o.isdeleted is false

                    union all

                    select 'PAYMENT'::varchar,
                           p.payment_date,
                           p.id,
                           coalesce(bp.name, sp.name),
                           concat_ws(' | ', p.payment_type, p.payment_method, p.note),
                           null::numeric,
                           null::numeric,
                           null::numeric,
                           p.amount::numeric,
                           p.status::varchar
                      from demo.payment p
                      left join demo.buying_party bp on bp.id = p.buying_party_id
                      left join demo.selling_party sp on sp.id = p.selling_party_id
                     where p.trip_id = :tripId
                       and p.isactive is true and p.isdeleted is false

                    union all

                    select 'EXPENSE'::varchar,
                           e.expense_date,
                           e.id,
                           sp.name,
                           concat_ws(' | ', e.category, e.note),
                           null::numeric,
                           null::numeric,
                           null::numeric,
                           e.amount::numeric,
                           null::varchar
                      from demo.expenses e
                      left join demo.selling_party sp on sp.id = e.selling_party_id
                     where e.trip_id = :tripId
                       and e.isactive is true and e.isdeleted is false
                ) activity
                order by report_date nulls last, record_type, reference_id
                """)
                .param("tripId", tripId)
                .query((rs, rowNum) -> {
                    Map<String, Object> record = new LinkedHashMap<>();
                    for (String column : COLUMNS) {
                        record.put(column, rs.getObject(column));
                    }
                    return record;
                })
                .list();

        BigDecimal orderValue = rows.stream()
                .filter(row -> "ORDER".equals(row.get("record_type")))
                .map(row -> decimal(row.get("amount")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal orderQuantity = rows.stream()
                .filter(row -> "ORDER".equals(row.get("record_type")))
                .map(row -> decimal(row.get("quantity")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReportData("Trip Summary Report", COLUMNS, rows, rows.size(), orderValue, orderQuantity);
    }

    private BigDecimal decimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal decimal) return decimal;
        return new BigDecimal(value.toString());
    }
}
