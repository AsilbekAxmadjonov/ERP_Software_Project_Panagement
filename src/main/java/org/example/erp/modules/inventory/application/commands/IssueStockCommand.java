package org.example.erp.modules.inventory.application.commands;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IssueStockCommand(long productId, BigDecimal quantity, String reference, LocalDate date) {}
