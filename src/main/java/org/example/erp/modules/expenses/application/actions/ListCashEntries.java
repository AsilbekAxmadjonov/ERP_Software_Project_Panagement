package org.example.erp.modules.expenses.application.actions;

import org.example.erp.modules.expenses.application.dto.CashEntryDTO;
import org.example.erp.modules.expenses.application.enums.EntryType;
import org.example.erp.modules.expenses.domain.CashEntryRepository;

import java.util.List;

public final class ListCashEntries {
    private final CashEntryRepository entries;

    public ListCashEntries(CashEntryRepository entries) {
        this.entries = entries;
    }

    public List<CashEntryDTO> handle(EntryType type) {
        return entries.findByKindNewestFirst(ExpensesMapper.toKind(type)).stream().map(ExpensesMapper::toDto).toList();
    }
}
