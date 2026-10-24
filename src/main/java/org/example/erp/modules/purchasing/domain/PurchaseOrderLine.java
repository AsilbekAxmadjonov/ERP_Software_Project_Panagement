package org.example.erp.modules.purchasing.domain;

import org.example.erp.platform.BusinessRuleException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record PurchaseOrderLine(long productId, String productName, String unit, BigDecimal quantity, BigDecimal unitCost) {

    public PurchaseOrderLine {
        if (quantity.signum() <= 0) throw new BusinessRuleException("Quantity must be greater than zero");
        if (unitCost.signum() < 0) throw new BusinessRuleException("Unit cost cannot be negative");
    }

    public BigDecimal lineTotal() {
        return quantity.multiply(unitCost).setScale(2, RoundingMode.HALF_UP);
    }
}
