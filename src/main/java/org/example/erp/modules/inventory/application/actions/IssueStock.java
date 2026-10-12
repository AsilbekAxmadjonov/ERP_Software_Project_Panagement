package org.example.erp.modules.inventory.application.actions;

import org.example.erp.modules.inventory.application.commands.IssueStockCommand;
import org.example.erp.modules.inventory.domain.MovementType;
import org.example.erp.modules.inventory.domain.Product;
import org.example.erp.modules.inventory.domain.ProductRepository;
import org.example.erp.modules.inventory.domain.StockMovement;
import org.example.erp.modules.inventory.domain.StockMovementRepository;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

import java.math.BigDecimal;

/** Takes goods out of stock for a sale and returns the unit cost they were valued at. */
public final class IssueStock {
    private final ProductRepository products;
    private final StockMovementRepository movements;
    private final TransactionRunner tx;

    public IssueStock(ProductRepository products, StockMovementRepository movements, TransactionRunner tx) {
        this.products = products;
        this.movements = movements;
        this.tx = tx;
    }

    public BigDecimal handle(IssueStockCommand c) {
        return tx.run(() -> {
            Product product = products.findById(c.productId()).orElseThrow(() -> new NotFoundException("Product not found"));
            BigDecimal unitCost = product.issue(c.quantity());
            Product saved = products.save(product);
            movements.save(StockMovement.record(saved, c.date(), MovementType.SALE, c.quantity().negate(), unitCost, c.reference(), "Sale"));
            return unitCost;
        });
    }
}
