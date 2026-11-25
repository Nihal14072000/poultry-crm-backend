package com.example.demo.service;

import com.example.demo.entity.SellingParty;
import com.example.demo.repository.SellingPartyRepository;
import com.example.demo.security.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SellingPartyService extends CrudService<SellingParty> {
    private final SellingPartyRepository repository;

    public SellingPartyService(SellingPartyRepository repository) {
        super(repository, SellingParty::setId);
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<SellingParty> findPage(int page, int size) {
        var pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Order.desc("createDate").nullsLast(), Sort.Order.desc("id")));
        return repository.findByTenantIdAndIsActiveTrueAndIsDeletedFalse(
                TenantContext.requireTenantId(), pageable);
    }
}
