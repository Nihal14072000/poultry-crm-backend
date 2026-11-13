package com.example.demo.service;

import com.example.demo.entity.Mortality;
import com.example.demo.repository.MortalityRepository;
import org.springframework.stereotype.Service;

@Service
public class MortalityService extends CrudService<Mortality> {
    public MortalityService(MortalityRepository repository) {
        super(repository, Mortality::setId);
    }
}
