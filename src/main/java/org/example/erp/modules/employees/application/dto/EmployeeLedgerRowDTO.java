package org.example.erp.modules.employees.application.dto;

import org.example.erp.modules.employees.application.enums.LedgerEntryKind;
import org.example.erp.modules.employees.application.enums.PaymentChannel;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * earned is what the entry adds to the amount owed (salary, bonus), settled what it takes off (payment, advance,
 * deduction); balance is the running amount owed after it. period ("2026-10") is set for salary accruals only,
 * method for payments and advances only.
 */
public record EmployeeLedgerRowDTO(long id, String reference, long employeeId, LocalDate date, LedgerEntryKind type,
                                   String period, PaymentChannel method, String description, BigDecimal earned,
                                   BigDecimal settled, BigDecimal balance, String createdBy) {}
