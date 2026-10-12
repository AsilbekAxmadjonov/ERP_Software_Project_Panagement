package org.example.erp.modules.inventory.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface StockMovementJpaRepository extends JpaRepository<StockMovementRecord, Long> {
    List<StockMovementRecord> findTop300ByOrderByIdDesc();

    List<StockMovementRecord> findByProductIdOrderByIdDesc(long productId);
}
