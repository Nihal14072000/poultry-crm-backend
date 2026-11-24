package com.example.demo.report;

import com.example.demo.dto.GeneratedReportResponse;
import com.example.demo.dto.ReportGenerationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.demo.security.TenantContext;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

@Service
public class ReportGenerationService {
    private static final Set<String> COMMON_FILTERS = Set.of("tripId", "partyId");
    private final ReportJobRepository jobs;
    private final JdbcClient jdbc;
    private final Path storageDirectory;

    public ReportGenerationService(ReportJobRepository jobs, JdbcClient jdbc,
            @Value("${reports.storage-dir:./storage/reports}") String storageDirectory) {
        this.jobs = jobs;
        this.jdbc = jdbc;
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    public GeneratedReportResponse generate(ReportGenerationRequest request, Integer userId) {
        validate(request);
        LocalDate fromDate = request.fromDate();
        LocalDate toDate = request.toDate();
        if (request.reportType() == ReportType.TRIP_SUMMARY) {
            LocalDate tripDate = findTripDate(integerFilter(request.filters(), "tripId"));
            fromDate = tripDate;
            toDate = tripDate;
        }
        Map<String, Object> filters = request.filters() == null ? Map.of() : request.filters();
        var existing = jobs.findMatching(request.reportType().name(), fromDate, toDate,
                request.format().name(), filters, userId);
        if (existing.isPresent()) {
            return existing.get();
        }
        long id = jobs.create(request.reportType().name(), fromDate, toDate,
                request.format().name(), filters, userId);
        return jobs.findResponse(id, userId).orElseThrow();
    }

    public java.util.List<GeneratedReportResponse> list(Integer userId) {
        return jobs.findRecentForUser(userId);
    }

    public GeneratedReportResponse get(Long id, Integer userId) {
        return jobs.findResponse(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
    }

    public ReportJob getOwnedJob(Long id, Integer userId) {
        return jobs.findOwnedJob(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
    }

    public boolean retry(Long id, Integer userId) {
        if (jobs.findOwnedJob(id, userId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found");
        }
        if (!jobs.retry(id, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only failed reports can be retried");
        }
        return true;
    }

    public Path storageDirectory() {
        return storageDirectory;
    }

    private void validate(ReportGenerationRequest request) {
        if (request.reportType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reportType is required");
        }
        Map<String, Object> filters = request.filters() == null ? Map.of() : request.filters();
        Set<String> allowed = switch (request.reportType()) {
            case SALES, DELIVERIES, PURCHASE -> Set.of("tripId", "partyId");
            case PAYMENTS -> Set.of("tripId", "partyId", "paymentType", "status");
            case OUTSTANDING -> Set.of("partyId");
            case TRIP_SUMMARY -> Set.of("tripId");
        };
        if (!allowed.containsAll(filters.keySet())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported filter supplied for report type");
        }

        if (request.reportType() == ReportType.TRIP_SUMMARY && filters.get("tripId") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tripId is required for TRIP_SUMMARY");
        }
        if (request.reportType() != ReportType.TRIP_SUMMARY) {
            if (request.fromDate() == null || request.toDate() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate and toDate are required");
            }
            if (request.fromDate().isAfter(request.toDate())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate must be on or before toDate");
            }
            if (ChronoUnit.DAYS.between(request.fromDate(), request.toDate()) > 1825) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report date range cannot exceed five years");
            }
        }

        for (String key : COMMON_FILTERS) {
            Object value = filters.get(key);
            if (value != null) {
                try {
                    if (Integer.parseInt(value.toString()) <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, key + " must be a positive integer");
                }
            }
        }
        validateEnumFilter(filters, "paymentType", Set.of("BUYER_RECEIPT", "SELLER_PAYOUT"));
        validateEnumFilter(filters, "status", Set.of("PENDING", "COMPLETED", "FAILED", "VOID"));
    }

    private LocalDate findTripDate(Integer tripId) {
        return jdbc.sql("select \"date\" from demo.trip where trip_id = :tripId and tenant_id = :tenantId")
                .param("tripId", tripId).param("tenantId", TenantContext.currentTenantId())
                .query(LocalDate.class)
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
    }

    private Integer integerFilter(Map<String, Object> filters, String key) {
        Object value = filters == null ? null : filters.get(key);
        if (value == null || value.toString().isBlank()) return null;
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, key + " must be a positive integer");
        }
    }

    private void validateEnumFilter(Map<String, Object> filters, String key, Set<String> allowed) {
        Object value = filters.get(key);
        if (value != null && !allowed.contains(value.toString().trim().toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + key + " filter");
        }
    }
}
