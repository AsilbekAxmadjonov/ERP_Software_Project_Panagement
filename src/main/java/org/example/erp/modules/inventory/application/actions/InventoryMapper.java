package org.example.erp.modules.inventory.application.actions;

import org.example.erp.modules.inventory.application.dto.ProductDTO;
import org.example.erp.modules.inventory.application.dto.StockMovementDTO;
import org.example.erp.modules.inventory.application.enums.MovementKind;
import org.example.erp.modules.inventory.application.enums.UnitOfMeasure;
import org.example.erp.modules.inventory.domain.Product;
import org.example.erp.modules.inventory.domain.StockMovement;
import org.example.erp.modules.inventory.domain.Unit;

final class InventoryMapper {
    private InventoryMapper() {}

    static ProductDTO toDto(Product p) {
        return new ProductDTO(p.getId(), p.getCode(), p.getName(), UnitOfMeasure.valueOf(p.getUnit().name()), p.getSalePrice(),
                p.getQuantity(), p.getAverageCost(), p.stockValue(), p.isActive());
    }

    static StockMovementDTO toDto(StockMovement m) {
        return new StockMovementDTO(m.id(), m.productId(), m.productName(), m.date(), MovementKind.valueOf(m.type().name()),
                m.quantity(), m.unitCost(), m.reference(), m.reason());
    }

    static Unit toDomain(UnitOfMeasure unit) {
        return Unit.valueOf(unit.name());
    }
}
