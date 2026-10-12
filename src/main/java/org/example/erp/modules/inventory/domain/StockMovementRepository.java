package org.example.erp.modules.inventory.domain;

import java.util.List;

public interface StockMovementRepository {
    StockMovement save(StockMovement movement);

    List<StockMovement> findRecent();

    List<StockMovement> findByProduct(long productId);
}
