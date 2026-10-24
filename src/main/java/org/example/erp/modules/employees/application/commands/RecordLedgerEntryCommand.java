package org.example.erp.modules.employees.application.commands;

import org.example.erp.modules.employees.application.enums.LedgerEntryKind;
import org.example.erp.modules.employees.application.enums.PaymentChannel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * date defaults to today. For a salary accrual, amount defaults to the monthly salary and period to the month of the
 * date; method is required for payments and advances only.
 */
public record RecordLedgerEntryCommand(long employeeId, LedgerEntryKind type, LocalDate date, BigDecimal amount,
                                       PaymentChannel method, YearMonth period, String description) {}
