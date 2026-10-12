package org.example.erp.modules.inventory.application.actions;

import org.example.erp.modules.inventory.application.commands.AdjustStockCommand;
import org.example.erp.modules.inventory.application.dto.ProductDTO;
import org.example.erp.modules.inventory.application.enums.StockChange;
import org.example.erp.modules.inventory.application.events.StockAdjusted;
import org.example.erp.modules.inventory.domain.MovementType;
import org.example.erp.modules.inventory.domain.Product;
import org.example.erp.modules.inventory.domain.ProductRepository;
import org.example.erp.modules.inventory.domain.StockMovement;
import org.example.erp.modules.inventory.domain.StockMovementRepository;
import org.example.erp.platform.BusinessRuleException;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

public final class AdjustStock {
    private final ProductRepository products;
    private final StockMovementRepository movements;
    private final IntegrationEventPublisher events;
    private final TransactionRunner tx;
    private final Clock clock;

    public AdjustStock(ProductRepository products, StockMovementRepository movements, IntegrationEventPublisher events,
                       TransactionRunner tx, Clock clock) {
        this.products = products;
        this.movements = movements;
        this.events = events;
        this.tx = tx;
        this.clock = clock;
    }

    public ProductDTO handle(AdjustStockCommand c) {
        if (c.reason() == null || c.reason().isBlank()) throw new BusinessRuleException("Reason is required");
        if (c.quantity().signum() == 0) throw new BusinessRuleException("Quantity change cannot be zero");
        BigDecimal quantity = c.quantity().setScale(3, RoundingMode.HALF_UP);

        return tx.run(() -> {
            Product product = products.findById(c.productId()).orElseThrow(() -> new NotFoundException("Product not found"));
            LocalDate today = LocalDate.now(clock);
            String reference = "ADJ-" + product.getCode();
            StockChange change;
            BigDecimal unitCost;
            BigDecimal value;

            if (quantity.signum() > 0) {
                if (c.unitCost() == null || c.unitCost().signum() < 0) throw new BusinessRuleException("Unit cost is required when adding stock");
                unitCost = c.unitCost();
                product.receive(quantity, unitCost);
                value = quantity.multiply(unitCost).setScale(2, RoundingMode.HALF_UP);
                change = StockChange.ADDED;
            } else {
                BigDecimal removed = quantity.negate();
                value = product.value(removed);
                unitCost = product.issue(removed);
                change = StockChange.WRITTEN_OFF;
            }

            Product saved = products.save(product);
            movements.save(StockMovement.record(saved, today, MovementType.ADJUSTMENT, quantity, unitCost, reference, c.reason()));
            if (value.signum() > 0) events.publish(new StockAdjusted(saved.getId(), saved.getName(), change, value, c.reason()));
            return InventoryMapper.toDto(saved);
        });
    }
}
