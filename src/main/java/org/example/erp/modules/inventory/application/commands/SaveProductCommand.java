package org.example.erp.modules.inventory.application.commands;

import org.example.erp.modules.inventory.application.enums.UnitOfMeasure;

import java.math.BigDecimal;

/** reorderLevel is optional: without it the product never raises a low-stock alert. */
public record SaveProductCommand(String code, String name, UnitOfMeasure unit, BigDecimal salePrice, boolean active,
                                 BigDecimal reorderLevel) {}
