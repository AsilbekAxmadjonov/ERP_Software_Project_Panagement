package org.example.erp.modules.expenses.application.contracts;

import org.example.erp.modules.expenses.application.enums.EntryType;

import java.util.List;

/** Host-supplied port: which ledger accounts categories may post to. Implemented in the composition layer. */
public interface LedgerAccounts {
    record AccountInfo(String code, String name) {}

    /** Active accounts an entry of this type can post to: expense accounts for expenses, revenue accounts for income. */
    List<AccountInfo> postable(EntryType type);
}
