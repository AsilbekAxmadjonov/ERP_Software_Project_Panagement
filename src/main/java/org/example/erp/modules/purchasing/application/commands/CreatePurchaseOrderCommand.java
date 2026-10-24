package org.example.erp.modules.purchasing.application.commands;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** date is optional: today is used when absent. */
public record CreatePurchaseOrderCommand(long supplierId, LocalDate date, List<Item> items) {
    public record Item(long productId, BigDecimal quantity, BigDecimal unitCost) {}
}
