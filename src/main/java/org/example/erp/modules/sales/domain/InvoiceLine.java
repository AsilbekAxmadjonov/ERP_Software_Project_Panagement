package org.example.erp.modules.sales.domain;

import org.example.erp.platform.BusinessRuleException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** returnedQuantity is how much of this line has been taken back on credit notes. */
public record InvoiceLine(long productId, String productName, String unit, BigDecimal quantity, BigDecimal unitPrice,
                          BigDecimal unitCost, BigDecimal returnedQuantity) {

    public InvoiceLine {
        if (quantity.signum() <= 0) throw new BusinessRuleException("Quantity must be greater than zero");
        if (unitPrice.signum() < 0) throw new BusinessRuleException("Price cannot be negative");
        if (returnedQuantity.signum() < 0 || returnedQuantity.compareTo(quantity) > 0)
            throw new BusinessRuleException("Returned quantity must be between zero and the sold quantity");
    }

    public InvoiceLine(long productId, String productName, String unit, BigDecimal quantity, BigDecimal unitPrice,
                       BigDecimal unitCost) {
        this(productId, productName, unit, quantity, unitPrice, unitCost, BigDecimal.ZERO.setScale(3));
    }

    public BigDecimal lineTotal() {
        return amountOf(quantity);
    }

    public BigDecimal lineCost() {
        return costOf(quantity);
    }

    public BigDecimal returnable() {
        return quantity.subtract(returnedQuantity);
    }

    BigDecimal amountOf(BigDecimal qty) {
        return qty.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
    }

    BigDecimal costOf(BigDecimal qty) {
        return qty.multiply(unitCost).setScale(2, RoundingMode.HALF_UP);
    }

    InvoiceLine withReturned(BigDecimal returned) {
        return new InvoiceLine(productId, productName, unit, quantity, unitPrice, unitCost, returned);
    }
}
