package org.example.erp.modules.inventory.application.actions;

import org.example.erp.modules.inventory.application.commands.ReceiveStockCommand;
import org.example.erp.modules.inventory.domain.MovementType;
import org.example.erp.modules.inventory.domain.Product;
import org.example.erp.modules.inventory.domain.ProductRepository;
import org.example.erp.modules.inventory.domain.StockMovement;
import org.example.erp.modules.inventory.domain.StockMovementRepository;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Brings goods into stock from a purchase receipt or a sales return at the given unit cost, re-averaging the
 * product's cost. Returns the value added to stock (quantity times unit cost, rounded to cents).
 */
public final class ReceiveStock {
    private record Movement(MovementType type, String reason) {}

    private final ProductRepository products;
    private final StockMovementRepository movements;
    private final TransactionRunner tx;

    public ReceiveStock(ProductRepository products, StockMovementRepository movements, TransactionRunner tx) {
        this.products = products;
        this.movements = movements;
        this.tx = tx;
    }

    public BigDecimal handle(ReceiveStockCommand c) {
        BigDecimal quantity = c.quantity().setScale(3, RoundingMode.HALF_UP);
        return tx.run(() -> {
            Product product = products.findById(c.productId()).orElseThrow(() -> new NotFoundException("Product not found"));
            product.receive(quantity, c.unitCost());
            Product saved = products.save(product);
            Movement movement = switch (c.source()) {
                case PURCHASE -> new Movement(MovementType.PURCHASE, "Purchase receipt");
                case SALES_RETURN -> new Movement(MovementType.SALES_RETURN, "Sales return");
            };
            movements.save(StockMovement.record(saved, c.date(), movement.type(), quantity, c.unitCost(), c.reference(),
                    movement.reason()));
            return quantity.multiply(c.unitCost()).setScale(2, RoundingMode.HALF_UP);
        });
    }
}
