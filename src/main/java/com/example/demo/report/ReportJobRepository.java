package com.example.demo.report;

import com.example.demo.dto.GeneratedReportResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ReportJobRepository {
    private final JdbcClient jdbc;
    private final ObjectMapper mapper;

    public ReportJobRepository(JdbcClient jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public long create(String type, LocalDate from, LocalDate to, String format,
                       Map<String, Object> filters, Integer requestedBy) {
        try {
            String json = mapper.writeValueAsString(filters == null ? Map.of() : filters);
            return jdbc.sql("""
                    insert into demo.generated_report
                        (report_type, from_date, to_date, report_format, filters, status, requested_by)
                    values (:type, :fromDate, :toDate, :format, cast(:filters as jsonb), 'QUEUED', :requestedBy)
                    returning id
                    """)
                    .param("type", type).param("fromDate", from).param("toDate", to)
                    .param("format", format).param("filters", json).param("requestedBy", requestedBy)
                    .query(Long.class).single();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create report job", ex);
        }
    }

    public Optional<GeneratedReportResponse> findMatching(
            String type, LocalDate from, LocalDate to, String format,
            Map<String, Object> filters, Integer requestedBy) {
        try {
            String json = mapper.writeValueAsString(filters == null ? Map.of() : filters);
            return jdbc.sql("""
                    select id, report_type, from_date, to_date, report_format, status,
                           file_name, error_message, created_at, completed_at
                    from demo.generated_report
                    where report_type = :type
                      and from_date = :fromDate
                      and to_date = :toDate
                      and report_format = :format
                      and filters = cast(:filters as jsonb)
                      and requested_by = :requestedBy
                    order by created_at desc
                    limit 1
                    """)
                    .param("type", type).param("fromDate", from).param("toDate", to)
                    .param("format", format).param("filters", json).param("requestedBy", requestedBy)
                    .query((rs, row) -> response(rs)).optional();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to find matching report job", ex);
        }
    }

    public List<GeneratedReportResponse> findRecentForUser(Integer userId) {
        return jdbc.sql("""
                select id, report_type, from_date, to_date, report_format, status,
                       file_name, error_message, created_at, completed_at
                from demo.generated_report
                where requested_by = :userId
                order by created_at desc
                limit 100
                """).param("userId", userId).query((rs, row) -> response(rs)).list();
    }

    public Optional<GeneratedReportResponse> findResponse(Long id, Integer userId) {
        return jdbc.sql("""
                select id, report_type, from_date, to_date, report_format, status,
                       file_name, error_message, created_at, completed_at
                from demo.generated_report
                where id = :id and requested_by = :userId
                """).param("id", id).param("userId", userId)
                .query((rs, row) -> response(rs)).optional();
    }

    public Optional<ReportJob> findOwnedJob(Long id, Integer userId) {
        return jdbc.sql("""
                select * from demo.generated_report where id = :id and requested_by = :userId
                """).param("id", id).param("userId", userId)
                .query((rs, row) -> job(rs)).optional();
    }

    public Optional<ReportJob> claimNext() {
        return jdbc.sql("""
                update demo.generated_report
                   set status = 'PROCESSING', started_at = current_timestamp, error_message = null
                 where id = (
                    select id from demo.generated_report
                     where status = 'QUEUED'
                     order by created_at
                     for update skip locked
                     limit 1
                 )
                returning *
                """).query((rs, row) -> job(rs)).optional();
    }

    public void markCompleted(Long id, String path, String fileName) {
        jdbc.sql("""
                update demo.generated_report
                   set status = 'COMPLETED', file_path = :path, file_name = :fileName,
                       completed_at = current_timestamp, error_message = null
                 where id = :id
                """).param("path", path).param("fileName", fileName).param("id", id).update();
    }

    public void markFailed(Long id, String message) {
        jdbc.sql("""
                update demo.generated_report
                   set status = 'FAILED', error_message = :message, completed_at = current_timestamp
                 where id = :id
                """).param("message", message).param("id", id).update();
    }

    public void requeueStaleProcessing() {
        jdbc.sql("""
                update demo.generated_report
                   set status = 'QUEUED', started_at = null
                 where status = 'PROCESSING'
                   and started_at < current_timestamp - interval '1 hour'
                """).update();
    }

    public boolean retry(Long id, Integer userId) {
        return jdbc.sql("""
                update demo.generated_report
                   set status = 'QUEUED', error_message = null, file_path = null, file_name = null,
                       started_at = null, completed_at = null
                 where id = :id and requested_by = :userId and status = 'FAILED'
                """).param("id", id).param("userId", userId).update() == 1;
    }

    private GeneratedReportResponse response(ResultSet rs) throws SQLException {
        return new GeneratedReportResponse(rs.getLong("id"), rs.getString("report_type"),
                rs.getObject("from_date", LocalDate.class), rs.getObject("to_date", LocalDate.class),
                rs.getString("report_format"), rs.getString("status"), rs.getString("file_name"),
                rs.getString("error_message"), rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("completed_at") == null ? null : rs.getTimestamp("completed_at").toLocalDateTime());
    }

    private ReportJob job(ResultSet rs) throws SQLException {
        Map<String, Object> filters;
        try {
            String json = rs.getString("filters");
            filters = json == null ? Map.of() : mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            filters = Map.of();
        }
        return new ReportJob(rs.getLong("id"), rs.getString("report_type"),
                rs.getObject("from_date", LocalDate.class), rs.getObject("to_date", LocalDate.class),
                rs.getString("report_format"), rs.getString("status"), filters, rs.getInt("requested_by"),
                rs.getString("file_path"), rs.getString("file_name"), rs.getString("error_message"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("completed_at") == null ? null : rs.getTimestamp("completed_at").toLocalDateTime());
    }
}
