package com.example.demo.service;

import com.example.demo.entity.ActiveRecord;
import com.example.demo.entity.CreatedRecord;
import com.example.demo.entity.TimestampedRecord;
import com.example.demo.entity.TenantScopedRecord;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class CrudService<T extends CreatedRecord> {

    private final JpaRepository<T, Integer> repository;
    private final BiConsumer<T, Integer> idSetter;

    public CrudService(JpaRepository<T, Integer> repository, BiConsumer<T, Integer> idSetter) {
        this.repository = repository;
        this.idSetter = idSetter;
    }

    @Transactional(readOnly = true)
    public List<T> findAll() {
        return findAll(Sort.by(Sort.Order.desc("createDate").nullsLast()));
    }

    @Transactional(readOnly = true)
    protected List<T> findAll(Sort sort) {
        return repository.findAll(sort).stream()
                .filter(record -> !(record instanceof ActiveRecord activeRecord)
                        || ActiveRecord.isActiveRecord(activeRecord))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<T> findById(Integer id) {
        return repository.findById(id)
                .filter(record -> !(record instanceof ActiveRecord activeRecord)
                        || ActiveRecord.isActiveRecord(activeRecord));
    }

    @Transactional
    public T create(T entity) {
        // All CRUD-managed entities use generated identifiers. Never allow a
        // request body to turn a create operation into an update of an existing row.
        idSetter.accept(entity, null);
        if (entity instanceof TimestampedRecord record) {
            AuditSupport.markCreated(record);
        }
        return repository.save(entity);
    }

    @Transactional
    public Optional<T> update(Integer id, T entity) {
        Optional<T> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        idSetter.accept(entity, id);
        if (entity instanceof TenantScopedRecord tenantRecord
                && existing.get() instanceof TenantScopedRecord existingTenant) {
            tenantRecord.setTenantId(existingTenant.getTenantId());
        }
        if (entity instanceof ActiveRecord record && existing.get() instanceof ActiveRecord existingRecord) {
            record.setIsActive(existingRecord.getIsActive());
            record.setIsDeleted(existingRecord.getIsDeleted());
        }
        if (entity instanceof TimestampedRecord record && existing.get() instanceof TimestampedRecord existingRecord) {
            AuditSupport.markUpdated(record, existingRecord);
        }
        return Optional.of(repository.save(entity));
    }

    @Transactional
    public boolean delete(Integer id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}
