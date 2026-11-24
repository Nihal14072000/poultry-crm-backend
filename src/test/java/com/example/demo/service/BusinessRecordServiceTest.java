package com.example.demo.service;

import com.example.demo.entity.BusinessRecord;
import com.example.demo.repository.BusinessRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BusinessRecordServiceTest {
    @Test
    void persistsRecordsWithoutClientSuppliedTenantMetadata() {
        BusinessRecordRepository repository = mock(BusinessRecordRepository.class);
        when(repository.save(any(BusinessRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        BusinessRecordService service = new BusinessRecordService(repository);

        Map<String, Object> result = service.create("farms", Map.of(
                "name", "North Farm",
                "_demoId", "client-controlled-id",
                "tenantId", "another-tenant",
                "tenant_id", "another-tenant"
        ));

        assertEquals("North Farm", result.get("name"));
        assertNotEquals("client-controlled-id", result.get("_demoId"));
        verify(repository).save(argThat(record ->
                record.getData().containsKey("name")
                        && !record.getData().containsKey("_demoId")
                        && !record.getData().containsKey("tenantId")
                        && !record.getData().containsKey("tenant_id")));
    }

    @Test
    void rejectsResourcePathTraversal() {
        BusinessRecordService service = new BusinessRecordService(mock(BusinessRecordRepository.class));

        assertThrows(ResponseStatusException.class, () -> service.get("../user_account"));
    }

    @Test
    void returnsAnEmptyDashboardForANewTenant() {
        BusinessRecordService service = new BusinessRecordService(mock(BusinessRecordRepository.class));

        Map<String, Object> dashboard = service.get("dashboard");

        assertTrue(((java.util.List<?>) dashboard.get("metrics")).isEmpty());
        assertTrue(((java.util.List<?>) dashboard.get("farmPerformance")).isEmpty());
    }
}
