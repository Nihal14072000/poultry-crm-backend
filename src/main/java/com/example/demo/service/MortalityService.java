package com.example.demo.service;

import com.example.demo.entity.ActiveRecord;
import com.example.demo.entity.CreatedRecord;
import com.example.demo.entity.Mortality;
import com.example.demo.repository.MortalityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MortalityService extends CrudService<Mortality> {
    private static final Logger log = LoggerFactory.getLogger(MortalityService.class);
    private final MortalityRepository repository;

    public MortalityService(MortalityRepository repository) {
        super(repository, Mortality::setId);
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Mortality> findByTripId(Integer tripId) {
        long tenantId = com.example.demo.security.TenantContext.requireTenantId();
        List<Mortality> mortalities = CreatedRecord.newestFirst(repository.findByTripId(tripId).stream()
                .filter(ActiveRecord::isActiveRecord)
                .filter(m -> java.util.Objects.equals(m.getTenantId(), tenantId))
                .toList());
        log.info("Retrieved {} active mortality records for tripId={}", mortalities.size(), tripId);
        return mortalities;
    }
}
