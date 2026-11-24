package com.example.demo.service;

import com.example.demo.entity.BusinessRecord;
import com.example.demo.repository.BusinessRecordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class BusinessRecordService {
    private static final Pattern RESOURCE_NAME = Pattern.compile("[a-z][a-z0-9-]{0,63}");
    private final BusinessRecordRepository records;

    public BusinessRecordService(BusinessRecordRepository records) {
        this.records = records;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> get(String resource) {
        validateResource(resource);
        if ("dashboard".equals(resource)) {
            return Map.of("metrics", List.of(), "revenueTrend", List.of(),
                    "farmPerformance", List.of(), "alerts", List.of(), "recentActivity", List.of());
        }
        List<Map<String, Object>> result = records.findAllByResourceOrderByUpdatedAtDesc(resource)
                .stream().map(this::toRecord).toList();
        return Map.of("records", result);
    }

    @Transactional
    public Map<String, Object> create(String resource, Map<String, Object> input) {
        validateResource(resource);
        Map<String, Object> data = sanitize(input);
        BusinessRecord saved = records.save(new BusinessRecord(UUID.randomUUID(), resource, data));
        return toRecord(saved);
    }

    @Transactional
    public Map<String, Object> update(String resource, UUID id, Map<String, Object> input) {
        validateResource(resource);
        BusinessRecord record = records.findById(id)
                .filter(existing -> existing.getResource().equals(resource))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
        record.setData(sanitize(input));
        return toRecord(records.save(record));
    }

    @Transactional
    public void delete(String resource, UUID id) {
        validateResource(resource);
        BusinessRecord record = records.findById(id)
                .filter(existing -> existing.getResource().equals(resource))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
        records.delete(record);
    }

    private Map<String, Object> sanitize(Map<String, Object> input) {
        if (input == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Record body is required");
        Map<String, Object> data = new LinkedHashMap<>(input);
        data.remove("_demoId");
        data.remove("tenantId");
        data.remove("tenant_id");
        return data;
    }

    private Map<String, Object> toRecord(BusinessRecord record) {
        Map<String, Object> result = new LinkedHashMap<>(record.getData());
        result.put("_demoId", record.getId().toString());
        return result;
    }

    private void validateResource(String resource) {
        if (resource == null || !RESOURCE_NAME.matcher(resource).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid resource name");
        }
    }
}
