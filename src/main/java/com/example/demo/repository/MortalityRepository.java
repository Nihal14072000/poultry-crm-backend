package com.example.demo.repository;

import com.example.demo.entity.Mortality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MortalityRepository extends JpaRepository<Mortality, Integer> {
    List<Mortality> findByTripId(Integer tripId);
}
