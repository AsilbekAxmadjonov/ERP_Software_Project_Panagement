package org.example.erp.modules.sales.domain;

import org.example.erp.platform.BusinessRuleException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record InvoiceLine(long productId, String productName, String unit, BigDecimal quantity, BigDecimal unitPrice,
                          BigDecimal unitCost) {

    public InvoiceLine {
        if (quantity.signum() <= 0) throw new BusinessRuleException("Quantity must be greater than zero");
        if (unitPrice.signum() < 0) throw new BusinessRuleException("Price cannot be negative");
    }

    public BigDecimal lineTotal() {
        return quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal lineCost() {
        return quantity.multiply(unitCost).setScale(2, RoundingMode.HALF_UP);
    }
}
