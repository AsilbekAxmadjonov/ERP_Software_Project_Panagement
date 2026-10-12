package org.example.erp.modules.inventory.application.commands;

import java.math.BigDecimal;

/** Positive quantity adds stock at unitCost, negative quantity writes stock off at average cost. */
public record AdjustStockCommand(long productId, BigDecimal quantity, BigDecimal unitCost, String reason) {}
