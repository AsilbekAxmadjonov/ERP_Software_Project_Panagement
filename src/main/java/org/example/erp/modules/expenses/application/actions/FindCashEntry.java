package org.example.erp.modules.expenses.application.actions;

import org.example.erp.modules.expenses.application.dto.CashEntryDTO;
import org.example.erp.modules.expenses.domain.CashEntryRepository;
import org.example.erp.platform.NotFoundException;

public final class FindCashEntry {
    private final CashEntryRepository entries;

    public FindCashEntry(CashEntryRepository entries) {
        this.entries = entries;
    }

    public CashEntryDTO handle(long id) {
        return entries.findById(id).map(ExpensesMapper::toDto).orElseThrow(() -> new NotFoundException("Entry not found"));
    }
}
