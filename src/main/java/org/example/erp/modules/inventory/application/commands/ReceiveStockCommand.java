package org.example.erp.modules.inventory.application.commands;

import org.example.erp.modules.inventory.application.enums.StockReceipt;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceiveStockCommand(long productId, BigDecimal quantity, BigDecimal unitCost, StockReceipt source,
                                  String reference, LocalDate date) {}
