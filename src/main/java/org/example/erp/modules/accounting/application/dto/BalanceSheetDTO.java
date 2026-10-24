package org.example.erp.modules.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Position as of a date. currentEarnings is revenue minus expenses not yet closed to an equity account; it is part
 * of totalEquity so that totalAssets = totalLiabilities + totalEquity whenever the ledger is balanced.
 */
public record BalanceSheetDTO(LocalDate asOf, List<StatementLineDTO> assets, BigDecimal totalAssets,
                              List<StatementLineDTO> liabilities, BigDecimal totalLiabilities,
                              List<StatementLineDTO> equity, BigDecimal currentEarnings, BigDecimal totalEquity,
                              BigDecimal totalLiabilitiesAndEquity, boolean balanced) {}
