package org.example.erp.modules.expenses.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Posted (not cancelled) entries dated in the period, totalled per category; net = income - expenses. */
public record CategoryReportDTO(LocalDate from, LocalDate to, List<Row> expenses, List<Row> income, BigDecimal totalExpenses,
                                BigDecimal totalIncome, BigDecimal net) {
    public record Row(long categoryId, String categoryName, String accountCode, long count, BigDecimal total) {}
}
