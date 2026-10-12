package org.example.erp.modules.inventory.application.dto;

import org.example.erp.modules.inventory.application.enums.UnitOfMeasure;

import java.math.BigDecimal;

public record ProductDTO(long id, String code, String name, UnitOfMeasure unit, BigDecimal salePrice, BigDecimal quantity,
                         BigDecimal averageCost, BigDecimal stockValue, boolean active) {}
