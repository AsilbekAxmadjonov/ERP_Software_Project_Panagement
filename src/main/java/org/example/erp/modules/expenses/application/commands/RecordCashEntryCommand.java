package org.example.erp.modules.expenses.application.commands;

import org.example.erp.modules.expenses.application.enums.EntryType;
import org.example.erp.modules.expenses.application.enums.PaymentChannel;

import java.math.BigDecimal;
import java.time.LocalDate;

/** date is optional: today is used when absent. */
public record RecordCashEntryCommand(EntryType type, long categoryId, LocalDate date, BigDecimal amount, PaymentChannel method,
                                     String description) {}
