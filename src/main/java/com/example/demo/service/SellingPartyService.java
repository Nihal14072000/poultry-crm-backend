package com.example.demo.service;

import com.example.demo.entity.SellingParty;
import com.example.demo.repository.SellingPartyRepository;
import org.springframework.stereotype.Service;

@Service
public class SellingPartyService extends CrudService<SellingParty> {
    public SellingPartyService(SellingPartyRepository repository) {
        super(repository, SellingParty::setId);
    }
}
