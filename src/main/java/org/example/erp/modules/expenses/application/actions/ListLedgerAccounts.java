package org.example.erp.modules.expenses.application.actions;

import org.example.erp.modules.expenses.application.contracts.LedgerAccounts;
import org.example.erp.modules.expenses.application.dto.LedgerAccountDTO;
import org.example.erp.modules.expenses.application.enums.EntryType;

import java.util.List;

/** The accounts a category of the given type can be mapped to. */
public final class ListLedgerAccounts {
    private final LedgerAccounts ledger;

    public ListLedgerAccounts(LedgerAccounts ledger) {
        this.ledger = ledger;
    }

    public List<LedgerAccountDTO> handle(EntryType type) {
        return ledger.postable(type).stream().map(a -> new LedgerAccountDTO(a.code(), a.name())).toList();
    }
}
