package com.example.demo.service;

import com.example.demo.entity.BuyingParty;
import com.example.demo.repository.BuyingPartyRepository;
import com.example.demo.security.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuyingPartyService extends CrudService<BuyingParty> {
    private final BuyingPartyRepository repository;

    public BuyingPartyService(BuyingPartyRepository repository) {
        super(repository, BuyingParty::setId);
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<BuyingParty> findPage(int page, int size) {
        var pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Order.desc("createDate").nullsLast(), Sort.Order.desc("id")));
        return repository.findByTenantIdAndIsActiveTrueAndIsDeletedFalse(
                TenantContext.requireTenantId(), pageable);
    }
}
