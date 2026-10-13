package org.example.erp.modules.inventory.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Quantity is signed: positive adds stock, negative removes it. */
public record StockMovement(Long id, long productId, String productName, LocalDate date, MovementType type,
                            BigDecimal quantity, BigDecimal unitCost, String reference, String reason) {

    public static StockMovement record(Product product, LocalDate date, MovementType type, BigDecimal quantity,
                                       BigDecimal unitCost, String reference, String reason) {
        return new StockMovement(null, product.getId(), product.getName(), date, type, quantity, unitCost, reference, reason);
    }
}
