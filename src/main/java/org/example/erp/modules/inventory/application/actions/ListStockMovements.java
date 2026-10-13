package org.example.erp.modules.inventory.application.actions;

import org.example.erp.modules.inventory.application.dto.StockMovementDTO;
import org.example.erp.modules.inventory.domain.StockMovementRepository;

import java.util.List;

public final class ListStockMovements {
    private final StockMovementRepository movements;

    public ListStockMovements(StockMovementRepository movements) {
        this.movements = movements;
    }

    public List<StockMovementDTO> handle(Long productId) {
        var found = productId == null ? movements.findRecent() : movements.findByProduct(productId);
        return found.stream().map(InventoryMapper::toDto).toList();
    }
}
