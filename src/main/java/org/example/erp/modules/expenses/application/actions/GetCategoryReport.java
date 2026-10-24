package org.example.erp.modules.expenses.application.actions;

import org.example.erp.modules.expenses.application.dto.CategoryReportDTO;
import org.example.erp.modules.expenses.application.dto.CategoryReportDTO.Row;
import org.example.erp.modules.expenses.domain.CashEntry;
import org.example.erp.modules.expenses.domain.CashEntryRepository;
import org.example.erp.modules.expenses.domain.CategoryKind;
import org.example.erp.modules.expenses.domain.EntryStatus;
import org.example.erp.platform.BusinessRuleException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Totals per category for a period; cancelled entries are left out. */
public final class GetCategoryReport {
    private final CashEntryRepository entries;

    public GetCategoryReport(CashEntryRepository entries) {
        this.entries = entries;
    }

    /** Both bounds are optional and inclusive. */
    public CategoryReportDTO handle(LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) throw new BusinessRuleException("The period ends before it starts");
        List<CashEntry> posted = entries.findDatedBetween(from, to).stream().filter(e -> e.getStatus() == EntryStatus.POSTED).toList();
        List<Row> expenses = totals(posted, CategoryKind.EXPENSE);
        List<Row> income = totals(posted, CategoryKind.INCOME);
        BigDecimal totalExpenses = sum(expenses);
        BigDecimal totalIncome = sum(income);
        return new CategoryReportDTO(from, to, expenses, income, totalExpenses, totalIncome, totalIncome.subtract(totalExpenses));
    }

    private static List<Row> totals(List<CashEntry> posted, CategoryKind kind) {
        Map<Long, Row> rows = new LinkedHashMap<>();
        for (CashEntry e : posted) {
            if (e.getKind() != kind) continue;
            rows.merge(e.getCategoryId(), new Row(e.getCategoryId(), e.getCategoryName(), e.getAccountCode(), 1, e.getAmount()),
                    (a, b) -> new Row(a.categoryId(), a.categoryName(), a.accountCode(), a.count() + 1, a.total().add(b.total())));
        }
        return rows.values().stream().sorted(Comparator.comparing(Row::total).reversed().thenComparing(Row::categoryName)).toList();
    }

    private static BigDecimal sum(List<Row> rows) {
        return rows.stream().map(Row::total).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }
}
