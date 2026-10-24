package org.example.erp.modules.sales.application.commands;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** date (today when absent) and reason are optional. */
public record IssueCreditNoteCommand(long invoiceId, LocalDate date, String reason, List<Item> items) {
    public record Item(long productId, BigDecimal quantity) {}
}
