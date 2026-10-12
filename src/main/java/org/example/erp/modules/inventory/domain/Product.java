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

    private Product(Long id, String code, String name, Unit unit, BigDecimal salePrice, BigDecimal quantity,
                    BigDecimal averageCost, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.salePrice = salePrice;
        this.quantity = quantity;
        this.averageCost = averageCost;
        this.active = active;
    }

    public static Product create(String code, String name, Unit unit, BigDecimal salePrice) {
        return new Product(null, code, name, unit, salePrice, BigDecimal.ZERO.setScale(3), BigDecimal.ZERO.setScale(4), true);
    }

    public static Product restore(Long id, String code, String name, Unit unit, BigDecimal salePrice, BigDecimal quantity,
                                  BigDecimal averageCost, boolean active) {
        return new Product(id, code, name, unit, salePrice, quantity, averageCost, active);
    }

    public void update(String name, Unit unit, BigDecimal salePrice, boolean active) {
        if (salePrice.signum() < 0) throw new BusinessRuleException("Price cannot be negative");
        this.name = name;
        this.unit = unit;
        this.salePrice = salePrice;
        this.active = active;
    }

    public BigDecimal stockValue() {
        return value(quantity);
    }

    public BigDecimal value(BigDecimal qty) {
        return qty.multiply(averageCost).setScale(2, RoundingMode.HALF_UP);
    }

    public void receive(BigDecimal qty, BigDecimal unitCost) {
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
