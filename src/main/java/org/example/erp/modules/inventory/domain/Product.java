package org.example.erp.modules.inventory.domain;

import lombok.Getter;
import org.example.erp.platform.BusinessRuleException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Stock is valued at weighted average cost. Quantity and cost only change through the methods below. */
@Getter
public final class Product {
    private final Long id;
    private final String code;
    private String name;
    private Unit unit;
    private BigDecimal salePrice;
    private BigDecimal quantity;
    private BigDecimal averageCost;
    private boolean active;
    private BigDecimal reorderLevel;

    private Product(Long id, String code, String name, Unit unit, BigDecimal salePrice, BigDecimal quantity,
                    BigDecimal averageCost, boolean active, BigDecimal reorderLevel) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.salePrice = salePrice;
        this.quantity = quantity;
        this.averageCost = averageCost;
        this.active = active;
        this.reorderLevel = reorderLevel;
    }

    public static Product create(String code, String name, Unit unit, BigDecimal salePrice, BigDecimal reorderLevel) {
        validate(salePrice, reorderLevel);
        return new Product(null, code, name, unit, salePrice, BigDecimal.ZERO.setScale(3), BigDecimal.ZERO.setScale(4), true,
                reorderLevel);
    }

    public static Product restore(Long id, String code, String name, Unit unit, BigDecimal salePrice, BigDecimal quantity,
                                  BigDecimal averageCost, boolean active, BigDecimal reorderLevel) {
        return new Product(id, code, name, unit, salePrice, quantity, averageCost, active, reorderLevel);
    }

    /** reorderLevel is null when the product has no minimum quantity. */
    public void update(String name, Unit unit, BigDecimal salePrice, boolean active, BigDecimal reorderLevel) {
        validate(salePrice, reorderLevel);
        this.name = name;
        this.unit = unit;
        this.salePrice = salePrice;
        this.active = active;
        this.reorderLevel = reorderLevel;
    }

    private static void validate(BigDecimal salePrice, BigDecimal reorderLevel) {
        if (salePrice.signum() < 0) throw new BusinessRuleException("Price cannot be negative");
        if (reorderLevel != null && reorderLevel.signum() < 0) throw new BusinessRuleException("Reorder level cannot be negative");
    }

    /** At or below the reorder level; a product without a reorder level is never low. */
    public boolean isLowStock() {
        return reorderLevel != null && quantity.compareTo(reorderLevel) <= 0;
    }

    public BigDecimal stockValue() {
        return value(quantity);
    }

    public BigDecimal value(BigDecimal qty) {
        return qty.multiply(averageCost).setScale(2, RoundingMode.HALF_UP);
    }

    public void receive(BigDecimal qty, BigDecimal unitCost) {
        if (qty.signum() <= 0) throw new BusinessRuleException("Received quantity must be greater than zero");
        if (unitCost.signum() < 0) throw new BusinessRuleException("Unit cost cannot be negative");
        BigDecimal newQuantity = quantity.add(qty);
        BigDecimal totalCost = quantity.multiply(averageCost).add(qty.multiply(unitCost));
        this.averageCost = totalCost.divide(newQuantity, 4, RoundingMode.HALF_UP);
        this.quantity = newQuantity;
    }

    /** Removes stock at the current average cost and returns that unit cost. */
    public BigDecimal issue(BigDecimal qty) {
        if (quantity.compareTo(qty) < 0)
            throw new BusinessRuleException("Not enough stock of " + name + ": have " + quantity + ", need " + qty);
        this.quantity = quantity.subtract(qty);
        return averageCost;
    }
}
