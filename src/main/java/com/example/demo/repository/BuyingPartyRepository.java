package com.example.demo.repository;

import com.example.demo.entity.BuyingParty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyingPartyRepository extends JpaRepository<BuyingParty, Integer> {
}
