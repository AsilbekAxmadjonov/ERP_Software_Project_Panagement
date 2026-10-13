package org.example.erp.modules.sales.application.commands;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** date and an item's unitPrice are optional: today and the product's sale price are used when absent. */
public record IssueInvoiceCommand(long customerId, LocalDate date, List<Item> items) {
    public record Item(long productId, BigDecimal quantity, BigDecimal unitPrice) {}
}
