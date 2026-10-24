package org.example.erp.modules.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dashboard figures: cash and bank balances as of the end date; revenue and expenses (all accounts of those kinds)
 * within [from, to]; profit = revenue - expenses.
 */
public record AccountingSummaryDTO(LocalDate from, LocalDate to, BigDecimal cash, BigDecimal bank, BigDecimal revenue,
                                   BigDecimal expenses, BigDecimal profit) {}
