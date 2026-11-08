package com.example.demo.service;

import com.example.demo.entity.ActiveRecord;
import com.example.demo.entity.CreatedRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
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
        List<T> records = repository.findAll().stream()
                .filter(record -> !(record instanceof ActiveRecord activeRecord)
                        || ActiveRecord.isActiveRecord(activeRecord))
                .toList();
        return CreatedRecord.newestFirst(records);
    }

    @Transactional(readOnly = true)
    public Optional<T> findById(Integer id) {
        return repository.findById(id)
                .filter(record -> !(record instanceof ActiveRecord activeRecord)
                        || ActiveRecord.isActiveRecord(activeRecord));
    }

    @Transactional
    public T create(T entity) {
        return repository.save(entity);
    }

    @Transactional
    public Optional<T> update(Integer id, T entity) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        idSetter.accept(entity, id);
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
