package org.example.erp.modules.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LedgerRowDTO(long entryId, LocalDate date, String reference, String description,
                           BigDecimal debit, BigDecimal credit, BigDecimal balance) {}
