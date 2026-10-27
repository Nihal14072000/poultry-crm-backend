package com.example.demo.repository;

import com.example.demo.entity.Mortality;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MortalityRepository extends JpaRepository<Mortality, Integer> {
}
