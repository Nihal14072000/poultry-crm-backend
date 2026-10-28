package com.example.demo.repository;

import com.example.demo.entity.FinanceOrderSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FinanceOrderSettlementRepository extends JpaRepository<FinanceOrderSettlement, Integer> {
    List<FinanceOrderSettlement> findByTripIdAndStatusNot(Integer tripId, String status);
}
