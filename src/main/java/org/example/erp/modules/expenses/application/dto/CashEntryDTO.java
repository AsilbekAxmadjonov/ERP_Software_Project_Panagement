package org.example.erp.modules.expenses.application.dto;

import org.example.erp.modules.expenses.application.enums.EntryState;
import org.example.erp.modules.expenses.application.enums.EntryType;
import org.example.erp.modules.expenses.application.enums.PaymentChannel;

import java.math.BigDecimal;
import java.time.LocalDate;

/** number is the document reference used in the ledger, e.g. EXP-00012 or INC-00013. */
public record CashEntryDTO(long id, String number, EntryType type, long categoryId, String categoryName, String accountCode,
                           LocalDate date, BigDecimal amount, PaymentChannel method, String description, EntryState status,
                           LocalDate cancelledOn, String createdBy) {}
