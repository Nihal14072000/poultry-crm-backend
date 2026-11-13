package com.example.demo.service;

import com.example.demo.entity.BuyingParty;
import com.example.demo.repository.BuyingPartyRepository;
import org.springframework.stereotype.Service;

@Service
public class BuyingPartyService extends CrudService<BuyingParty> {
    public BuyingPartyService(BuyingPartyRepository repository) {
        super(repository, BuyingParty::setId);
    }
}
