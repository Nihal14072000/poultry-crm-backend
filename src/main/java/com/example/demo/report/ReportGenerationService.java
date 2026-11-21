package com.example.demo.report;

import com.example.demo.dto.GeneratedReportResponse;
import com.example.demo.dto.ReportGenerationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

@Service
public class ReportGenerationService {
    private static final Set<String> COMMON_FILTERS = Set.of("tripId", "partyId");
    private final ReportJobRepository jobs;
    private final Path storageDirectory;

    public ReportGenerationService(ReportJobRepository jobs,
            @Value("${reports.storage-dir:./storage/reports}") String storageDirectory) {
        this.jobs = jobs;
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    public GeneratedReportResponse generate(ReportGenerationRequest request, Integer userId) {
        validate(request);
        long id = jobs.create(request.reportType().name(), request.fromDate(), request.toDate(),
                request.format().name(), request.filters(), userId);
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
        if (request.fromDate().isAfter(request.toDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate must be on or before toDate");
        }
        if (ChronoUnit.DAYS.between(request.fromDate(), request.toDate()) > 1825) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report date range cannot exceed five years");
        }
        Map<String, Object> filters = request.filters() == null ? Map.of() : request.filters();
        Set<String> allowed = switch (request.reportType()) {
            case SALES, DELIVERIES -> Set.of("tripId", "partyId");
            case PURCHASE -> Set.of("tripId", "partyId");
            case PAYMENTS -> Set.of("tripId", "partyId", "paymentType", "status");
            case OUTSTANDING -> Set.of("partyId");
        };
        if (!allowed.containsAll(filters.keySet())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported filter supplied for report type");
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

    private void validateEnumFilter(Map<String, Object> filters, String key, Set<String> allowed) {
        Object value = filters.get(key);
        if (value != null && !allowed.contains(value.toString().trim().toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + key + " filter");
        }
    }
}
