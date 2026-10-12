package org.example.erp.modules.inventory.application.commands;

import org.example.erp.modules.inventory.application.enums.UnitOfMeasure;

import java.math.BigDecimal;

public record SaveProductCommand(String code, String name, UnitOfMeasure unit, BigDecimal salePrice, boolean active) {}
